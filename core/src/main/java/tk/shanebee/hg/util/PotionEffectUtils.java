package tk.shanebee.hg.util;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import tk.shanebee.hg.HG;

import java.util.Locale;

/**
 * Util for getting {@link PotionEffectType}
 * <p>Name resolution (new/legacy names) is done by the version adapter</p>
 */
public final class PotionEffectUtils {

    private PotionEffectUtils() {
    }

    /**
     * Get a PotionEffectType from a Minecraft key (e.g. jump_boost) or a legacy Bukkit name (e.g. JUMP)
     *
     * @param key Key for PotionEffectType, optionally prefixed by 'minecraft:'
     * @return PotionEffectType (null if it does not exist on this server version)
     */
    @Nullable
    public static PotionEffectType get(String key) {
        if (key == null) return null;
        String name = key.trim().toUpperCase(Locale.ROOT);
        if (name.startsWith("MINECRAFT:")) name = name.substring("MINECRAFT:".length());
        return HG.getPlugin().getAdapter().getPotionEffect(name);
    }

    /**
     * Get a PotionEffect from string
     * <p><b>Format:</b>
     * <br>POTION_EFFECT_TYPE:int(duration):int(amplifier)</p>
     *
     * @param data Data string for potion effect
     * @return New PotionEffect if checks passed
     */
    @Nullable
    public static PotionEffect getPotionEffect(String data) {
        String[] potionData = data.split(":");
        if (potionData.length == 3) {
            PotionEffectType type = get(potionData[0]);
            if (type == null) {
                potionWarning("Potion effect type not found: &c" + potionData[0].toUpperCase(Locale.ROOT) + " &ein: &b" + data);
                return null;
            } else if (!Util.isInt(potionData[1])) {
                potionWarning("Potion duration incorrect format: &c" + potionData[1] + " &ein: &b" + data);
                return null;
            } else if (!Util.isInt(potionData[2])) {
                potionWarning("Potion amplifier incorrect format: &c" + potionData[2] + " &ein: &b" + data);
                return null;
            }
            int duration = Integer.parseInt(potionData[1]);
            int amplifier = Integer.parseInt(potionData[2]);
            return new PotionEffect(type, duration, amplifier);
        } else {
            potionWarning("Improper setup of potion: &c" + data);
            return null;
        }
    }

    private static void potionWarning(@Nullable String warning) {
        if (warning != null) Util.warning(warning);
        Util.warning("&r  - Check your configs");
        Util.warning("&r  - Proper example:");
        Util.warning("      &bpotion:POTION_EFFECT_TYPE:DURATION_IN_TICKS:LEVEL");
        Util.warning("      &bpotion:HEAL:200:1");
    }

    public static void deprecationWarning(String data) {
        if (data.contains("potion:")) {
            Util.warning("&c'potion:'&e has been changed to &a'potion-type:'&e please update your configs. Found: &7" + data);
        }
    }
}