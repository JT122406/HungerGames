package tk.shanebee.adapters.legacy;

import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PotionUtils {
    private static final Map<String, String> NEW_TO_OLD = new HashMap<>();
    private static final Map<String, String> TYPE_NEW_TO_OLD = new HashMap<>();

    static {
        NEW_TO_OLD.put("JUMP_BOOST", "JUMP");
        NEW_TO_OLD.put("STRENGTH", "INCREASE_DAMAGE");
        NEW_TO_OLD.put("SLOWNESS", "SLOW");
        NEW_TO_OLD.put("HASTE", "FAST_DIGGING");
        NEW_TO_OLD.put("MINING_FATIGUE", "SLOW_DIGGING");
        NEW_TO_OLD.put("RESISTANCE", "DAMAGE_RESISTANCE");
        NEW_TO_OLD.put("INSTANT_HEALTH", "HEAL");
        NEW_TO_OLD.put("INSTANT_DAMAGE", "HARM");
        NEW_TO_OLD.put("NAUSEA", "CONFUSION");

        TYPE_NEW_TO_OLD.put("EMPTY", "UNCRAFTABLE");
        TYPE_NEW_TO_OLD.put("LEAPING", "JUMP");
        TYPE_NEW_TO_OLD.put("SWIFTNESS", "SPEED");
        TYPE_NEW_TO_OLD.put("HEALING", "INSTANT_HEAL");
        TYPE_NEW_TO_OLD.put("HARMING", "INSTANT_DAMAGE");
        TYPE_NEW_TO_OLD.put("REGENERATION", "REGEN");
    }

    private PotionUtils() {}

    public static PotionEffectType find(String name) {
        if (name == null) return null;
        String upper = name.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        PotionEffectType type = PotionEffectType.getByName(upper);
        if (type == null && NEW_TO_OLD.containsKey(upper)) type = PotionEffectType.getByName(NEW_TO_OLD.get(upper));
        return type;
    }

    public static Collection<PotionEffectType> all() {
        List<PotionEffectType> list = new ArrayList<>();
        for (PotionEffectType t : PotionEffectType.values()) {
            if (t != null) list.add(t);
        }
        return list;
    }

    public static PotionType findType(String name) {
        if (name == null) return null;
        String upper = name.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        PotionType type = byName(upper);
        if (type == null && TYPE_NEW_TO_OLD.containsKey(upper)) type = byName(TYPE_NEW_TO_OLD.get(upper));
        return type;
    }

    public static boolean setBasePotion(PotionMeta meta, PotionType type, boolean extended, boolean upgraded) {
        if (extended && !type.isExtendable()) return false;
        if (upgraded && !type.isUpgradeable()) return false;
        meta.setBasePotionData(new PotionData(type, extended, upgraded));
        return true;
    }

    private static PotionType byName(String name) {
        for (PotionType t : PotionType.values()) {
            if (t.name().equals(name)) return t;
        }
        return null;
    }
}