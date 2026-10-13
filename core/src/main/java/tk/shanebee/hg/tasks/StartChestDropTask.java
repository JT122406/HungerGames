package tk.shanebee.hg.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.ChestDrop;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.util.Util;

import java.util.UUID;

public class StartChestDropTask implements Runnable {
    private Game game;
    private Location dropLocation;
    private ChestDrop drop;

    public StartChestDropTask(Game game, ChestDrop drop) {
        this.game = game;
        this.dropLocation = drop.getLocation();
        this.drop = drop;
    }

    @Override
    public void run() {
        int x = dropLocation.getBlockX();
        int y = dropLocation.getBlockY();
        int z = dropLocation.getBlockZ();
        World w = game.getGameArenaData().getBound().getWorld();
        for (UUID u : game.getGamePlayerData().getPlayers()) {
            Player p = Bukkit.getPlayer(u);
            if (p != null) {
                Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
                Util.scm(p, HG.getPlugin().getLang().chest_drop_2
                        .replace("<x>", String.valueOf(x))
                        .replace("<y>", String.valueOf(y))
                        .replace("<z>", String.valueOf(z)));
                Util.scm(p, HG.getPlugin().getLang().chest_drop_1);
            }
        }
        drop.setCurDropTaskId(Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(HG.getPlugin(), new ChestDropTask(game, drop, w.getBlockAt(dropLocation.add(0, 10, 0)), 10), 600));
    }

}
