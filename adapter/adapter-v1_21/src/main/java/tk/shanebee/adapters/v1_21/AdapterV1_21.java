package tk.shanebee.adapters.v1_21;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import tk.shanebee.adapters.v1_17.AdapterV1_17;

import java.util.*;

public class AdapterV1_21 extends AdapterV1_17 {
    private Attribute maxHealthAttr;

    private Attribute maxHealth() {
        if (maxHealthAttr == null) {
            Attribute a = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health"));
            if (a == null) a = Registry.ATTRIBUTE.get(NamespacedKey.minecraft("generic.max_health"));
            maxHealthAttr = Objects.requireNonNull(a, "Attribute max health not found");
        }
        return maxHealthAttr;
    }

    @Override
    public double getMaxHealth(LivingEntity e) {
        return Objects.requireNonNull(e.getAttribute(maxHealth())).getValue();
    }

    @Override
    public double getBaseMaxHealth(LivingEntity e) {
        return Objects.requireNonNull(e.getAttribute(maxHealth())).getBaseValue();
    }

    @Override
    public void setMaxHealth(LivingEntity e, double value) {
        AttributeInstance a = e.getAttribute(maxHealth());
        if (a != null) a.setBaseValue(value);
    }

    private static final Map<String, String> OLD_TO_KEY = Map.ofEntries(
            Map.entry("JUMP", "jump_boost"),
            Map.entry("INCREASE_DAMAGE", "strength"),
            Map.entry("SLOW", "slowness"),
            Map.entry("FAST_DIGGING", "haste"),
            Map.entry("SLOW_DIGGING", "mining_fatigue"),
            Map.entry("DAMAGE_RESISTANCE", "resistance"),
            Map.entry("HEAL", "instant_health"),
            Map.entry("HARM", "instant_damage"),
            Map.entry("CONFUSION", "nausea")
    );

    @Override
    public PotionEffectType getPotionEffect(String name) {
        if (name == null) return null;
        String upper = name.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        String key = OLD_TO_KEY.getOrDefault(upper, upper.toLowerCase(Locale.ROOT));
        try {
            return Registry.EFFECT.get(NamespacedKey.minecraft(key));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static final Map<String, String> TYPE_OLD_TO_NEW = Map.ofEntries(
            Map.entry("UNCRAFTABLE", "EMPTY"),
            Map.entry("JUMP", "LEAPING"),
            Map.entry("SPEED", "SWIFTNESS"),
            Map.entry("INSTANT_HEAL", "HEALING"),
            Map.entry("INSTANT_DAMAGE", "HARMING"),
            Map.entry("REGEN", "REGENERATION")
    );

    @Override
    public PotionType getPotionType(String name) {
        if (name == null) return null;
        String upper = name.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        return potionType(TYPE_OLD_TO_NEW.getOrDefault(upper, upper));
    }

    @Override
    public boolean setBasePotion(PotionMeta meta, PotionType type, boolean extended, boolean upgraded) {
        PotionType variant = potionType((extended ? "LONG_" : upgraded ? "STRONG_" : "") + type.name());
        if (variant == null) return false;
        meta.setBasePotionType(variant);
        return true;
    }

    private static PotionType potionType(String name) {
        for (PotionType t : PotionType.values()) {
            if (t.name().equals(name)) return t;
        }
        return null;
    }

    private static final Map<String, String> ENCH_ALIAS = Map.ofEntries(
            Map.entry("DAMAGE_ALL", "sharpness"),
            Map.entry("DAMAGE_UNDEAD", "smite"),
            Map.entry("DAMAGE_ARTHROPODS", "bane_of_arthropods"),
            Map.entry("PROTECTION_ENVIRONMENTAL", "protection"),
            Map.entry("PROTECTION_FIRE", "fire_protection"),
            Map.entry("PROTECTION_FALL", "feather_falling"),
            Map.entry("PROTECTION_EXPLOSIONS", "blast_protection"),
            Map.entry("PROTECTION_PROJECTILE", "projectile_protection"),
            Map.entry("DURABILITY", "unbreaking"),
            Map.entry("DIG_SPEED", "efficiency"),
            Map.entry("ARROW_DAMAGE", "power"),
            Map.entry("ARROW_KNOCKBACK", "punch"),
            Map.entry("ARROW_FIRE", "flame"),
            Map.entry("ARROW_INFINITE", "infinity"),
            Map.entry("LOOT_BONUS_MOBS", "looting"),
            Map.entry("LOOT_BONUS_BLOCKS", "fortune"),
            Map.entry("SWEEPING_EDGE", "sweeping_edge")
    );

    @Override
    public Enchantment findEnchantment(String name) {
        if (name == null) return null;
        String n = name.trim();
        String alias = ENCH_ALIAS.get(n.toUpperCase(Locale.ROOT));
        try {
            return Registry.ENCHANTMENT.get(NamespacedKey.minecraft(alias != null ? alias : n.toLowerCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}