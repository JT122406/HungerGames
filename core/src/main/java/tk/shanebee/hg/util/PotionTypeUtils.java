package tk.shanebee.hg.util;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.Nullable;
import tk.shanebee.hg.HG;

import java.util.Locale;

/**
 * Util for getting {@link PotionType}
 * <p>Name resolution (new/legacy names) and applying the base potion are done by the version adapter</p>
 */
public final class PotionTypeUtils {

    private static final String LONG = "LONG_";
    private static final String STRONG = "STRONG_";

    private PotionTypeUtils() {
    }

    /**
     * Get a base PotionType from a Minecraft key (e.g. leaping) or a legacy Bukkit name (e.g. JUMP)
     *
     * @param key Key for PotionType, optionally prefixed by 'minecraft:'
     * @return PotionType (null if it does not exist on this server version)
     */
    @Nullable
    public static PotionType get(String key) {
        if (key == null) return null;
        String name = key.trim().toUpperCase(Locale.ROOT);
        if (name.startsWith("MINECRAFT:")) name = name.substring("MINECRAFT:".length());
        return HG.getPlugin().getAdapter().getPotionType(name);
    }

    /**
     * Apply a base potion to a PotionMeta from a String
     * <p><b>Formats:</b>
     * <br>POTION-TYPE (optional start with 'LONG_' or 'STRONG_')
     * <br>POTION-TYPE:boolean(strong):boolean(extended)</p>
     *
     * @param meta PotionMeta to apply the base potion to
     * @param data data string of potion type
     * @return true if the base potion was applied
     */
    public static boolean applyBasePotion(PotionMeta meta, String data) {
        String[] potionData = data.split(":");
        PotionType potionType;
        boolean upgraded;
        boolean extended;
        if (potionData.length == 1) {
            String pData = potionData[0].toUpperCase(Locale.ROOT);
            upgraded = pData.startsWith(STRONG);
            extended = pData.startsWith(LONG);
            if (upgraded) pData = pData.substring(STRONG.length());
            else if (extended) pData = pData.substring(LONG.length());
            potionType = get(pData);
            if (potionType == null) {
                Util.warning("Potion base type not found: &c" + potionData[0].toUpperCase(Locale.ROOT) + " &ein: &b" + data);
                Util.warning("&r  - Check your configs");
                Util.warning("&r  - Proper examples:");
                Util.warning("      &bpotion-base:turtle_master");
                Util.warning("      &bpotion-base:LONG_TURTLE_MASTER");
                Util.warning("      &bpotion-base:strong_turtle_master");
                return false;
            }
        } else if (potionData.length == 3) {
            potionType = get(potionData[0]);
            if (potionType == null) {
                potionTypeWarning("Potion base type not found: &c" + potionData[0].toUpperCase(Locale.ROOT) + " &ein: &b" + data);
                return false;
            } else if (!Util.isBool(potionData[1])) {
                potionTypeWarning("Not a valid boolean: &c" + potionData[1].toUpperCase(Locale.ROOT) + " &ein: &b" + data);
                return false;
            } else if (!Util.isBool(potionData[2])) {
                potionTypeWarning("Not a valid boolean: &c" + potionData[2].toUpperCase(Locale.ROOT) + " &ein: &b" + data);
                return false;
            }
            upgraded = Boolean.parseBoolean(potionData[1]);
            extended = Boolean.parseBoolean(potionData[2]);
            if (upgraded && extended) {
                Util.warning("Potion can not be both upgraded and extended in: &b" + data);
                return false;
            }
        } else {
            potionTypeWarning("Improper setup of potion-data: &c" + data);
            return false;
        }

        if (!HG.getPlugin().getAdapter().setBasePotion(meta, potionType, extended, upgraded)) {
            Util.warning("Potion can not be " + (extended ? "extended" : "upgraded") + ": &b" + data);
            return false;
        }
        return true;
    }

    private static void potionTypeWarning(@Nullable String warning) {
        if (warning != null) Util.warning(warning);
        Util.warning("&r  - Check your configs");
        Util.warning("&r  - Proper example:");
        Util.warning("      &bpotion-base:POTION_TYPE:UPGRADED:EXTENDED");
        Util.warning("      &bpotion-base:turtle_master:true:false");
    }

}