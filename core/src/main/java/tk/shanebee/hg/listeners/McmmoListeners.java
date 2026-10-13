package tk.shanebee.hg.listeners;

import com.gmail.nossr50.events.experience.McMMOPlayerExperienceEvent;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelDownEvent;
import com.gmail.nossr50.events.experience.McMMOPlayerLevelUpEvent;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import com.gmail.nossr50.events.fake.*;
import com.gmail.nossr50.events.items.McMMOItemSpawnEvent;
import com.gmail.nossr50.events.skills.abilities.McMMOPlayerAbilityActivateEvent;
import com.gmail.nossr50.events.skills.secondaryabilities.SubSkillEvent;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.managers.PlayerManager;

/**
 * Internal mcMMO listeners
 */
public class McmmoListeners implements Listener {

    // https://github.com/mcMMO-Dev/mcMMO/blob/058279c148a65bd708dc85067d3858f6adde1173/src/main/java/com/gmail/nossr50/util/MetadataConstants.java#L36
    private static final String MCMMO_CUSTOM_DAMAGE = "mcMMO: Custom Damage";

    private final HG plugin;
    private final PlayerManager playerManager;

    public McmmoListeners(HG plugin) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
    }

    // Handle mcMMO EXP gain events
    @EventHandler
    private void mcMMOLevelUp(McMMOPlayerLevelUpEvent event) {
        handleExpEvent(event);
    }

    @EventHandler
    private void mcMMOLevelDown(McMMOPlayerLevelDownEvent event) {
        handleExpEvent(event);
    }

    @EventHandler
    private void mcMMOXpGain(McMMOPlayerXpGainEvent event) {
        handleExpEvent(event);
    }

    private void handleExpEvent(McMMOPlayerExperienceEvent event) {
        if (!Config.mcmmoGainExp) {
            Player player = event.getPlayer();
            if (playerManager.hasPlayerData(player.getUniqueId())) {
                //if (playerManager.hasPlayerData(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    // Handle mcMMO skill use events
    @EventHandler
    private void mcMMOUseSkill(McMMOPlayerAbilityActivateEvent event) {
        if (!Config.mcmmoUseSkills) {
            Player player = event.getPlayer();
            if (playerManager.hasPlayerData(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void mcMMOUseSubSkill(SubSkillEvent event) {
        if (!Config.mcmmoUseSkills) {
            Player player = event.getPlayer();
            if (playerManager.hasPlayerData(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void blockBreakEvent(FakeBlockBreakEvent event) {
        if (!Config.mcmmoUseSkills) {
            Player player = event.getPlayer();
            if (playerManager.hasPlayerData(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    protected void blockDamageEvent(FakeBlockDamageEvent event) {
        if (!Config.mcmmoUseSkills) {
            Player player = event.getPlayer();
            if (playerManager.hasPlayerData(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    // Also receives EntityDamageByEntityEvent, which shares EntityDamageEvent's handler list
    @EventHandler
    private void entityDamageEvent(EntityDamageEvent event) {
        if (Config.mcmmoUseSkills) return;
        Entity victim = event.getEntity();
        if (!victim.hasMetadata(MCMMO_CUSTOM_DAMAGE)) return;

        if (playerManager.hasPlayerData(victim.getUniqueId())) {
            event.setCancelled(true);
        } else if (event instanceof EntityDamageByEntityEvent
                && playerManager.hasPlayerData(((EntityDamageByEntityEvent) event).getDamager().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    private void fishEvent(FakePlayerFishEvent event) {
        if (!Config.mcmmoUseSkills) {
            Player player = event.getPlayer();
            if (playerManager.hasPlayerData(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    private void itemSpawnEvent(McMMOItemSpawnEvent event) {
        if (!Config.mcmmoUseSkills) {
            Location loc = event.getLocation();
            plugin.getGames().stream().filter(game -> game.getGameArenaData().isInRegion(loc)).map(game -> true).forEach(event::setCancelled);
        }
    }

}