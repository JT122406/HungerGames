package tk.shanebee.hg.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.Language;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.util.Util;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Objects;
import java.util.UUID;

public class StartingTask implements Runnable {

    private int timer;
    private int step = 5;
    private int id;
    private final Game game;
    private final Language lang;

    public StartingTask(Game g) {
        this.timer = 30;
        this.game = g;
        this.lang = HG.getPlugin().getLang();
        String name = g.getGameArenaData().getName();
        String broadcast = lang.game_started
                .replace("<arena>", name)
                .replace("<seconds>", String.valueOf(timer));
        if (Config.broadcastJoinMessages) {
            Util.broadcast(broadcast);
            Util.broadcast(lang.game_join.replace("<arena>", name));
        } else game.getGamePlayerData().msgAll(broadcast);

        this.id = Bukkit.getScheduler().scheduleSyncRepeatingTask(HG.getPlugin(), this, 5 * 20L, 5 * 20L);
    }

    @Override
    public void run() {
        timer -= step;

        if (game.getGameArenaData().getStatus() != Status.COUNTDOWN) {
            stop();
            return;
        }

        // gentle pre-start heal: +3 health up to max
        for (UUID p : game.getGamePlayerData().getPlayers()) {
            Player player = Bukkit.getPlayer(p);
            if (player == null) continue;
            double max = Objects.requireNonNull(
                    player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
            ).getValue();
            double newHealth = Math.min(max, player.getHealth() + 3.0);
            player.setHealth(newHealth);
        }

        // switch to 1-second ticks for 5..4..3..2..1
        if (timer == 5 && step == 5) {
            Bukkit.getScheduler().cancelTask(id);
            step = 1;
            id = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    HG.getPlugin(), this, 20L, 20L
            );
        }

        if (timer <= 0) {
            // clear inventory on game start if either leave/forcestart items are enabled
            if (Config.enableleaveitem || Config.enableforcestartitem) {
                game.getGamePlayerData().getPlayers().forEach(uuid -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null) return;
                    if (player.getInventory().contains(Objects.requireNonNull(Material.getMaterial(Config.forcestartitem)))
                            || player.getInventory().contains(Objects.requireNonNull(Material.getMaterial(Config.leaveitemtype)))) {
                        player.getInventory().clear();
                    }
                });
            }

            // top off players
            for (UUID p : game.getGamePlayerData().getPlayers()) {
                Player player = Bukkit.getPlayer(p);
                if (player == null) continue;
                double max = Objects.requireNonNull(
                        player.getAttribute(Attribute.GENERIC_MAX_HEALTH)
                ).getValue();
                player.setHealth(max);
                player.setSaturation(20);
                player.setFoodLevel(20);
            }

            // dragon roar + speed boost at start
            PotionEffect speedBoost = new PotionEffect(PotionEffectType.SPEED, 5 * 20, 1, false, true);
            for (UUID p : game.getGamePlayerData().getPlayers()) {
                Player player = Bukkit.getPlayer(p);
                if (player == null) continue;
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);
                player.addPotionEffect(speedBoost);
            }

            game.startFreeRoam();
            stop();
        } else {
            game.getGamePlayerData().msgAll(
                    lang.game_countdown.replace("<timer>", String.valueOf(timer))
            );

            if (step == 1) { // play note each last 5..4..3..2..1
                for (UUID p : game.getGamePlayerData().getPlayers()) {
                    Player player = Bukkit.getPlayer(p);
                    if (player == null) continue;
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 2f);
                }
            }
        }
    }

    public void stop() {
        Bukkit.getScheduler().cancelTask(id);
    }

}
