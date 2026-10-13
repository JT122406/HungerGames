package tk.shanebee.adapters.legacy;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import tk.shanebee.adapters.VersionAdapter;

import java.util.Locale;
import java.util.Objects;

public class AdapterLegacy implements VersionAdapter {
    @Override
    public BlockState getBlockState(Block block) {
        return block.getState();
    }

    @Override
    public String getWorldId(World world) {
        return world.getName();
    }

    @Override
    public BlockState getBlockSnapshot(Block block) {
        return block.getState();
    }

    @Override
    public World findWorld(String id) {
        if (id == null) return null;
        World w = Bukkit.getWorld(id);
        if (w == null) {
            int s = id.indexOf('/');
            if (s > 0) w = Bukkit.getWorld(id.substring(s + 1));
        }
        return w;
    }

    @Override
    public double getMaxHealth(LivingEntity e) {
        return Objects.requireNonNull(e.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
    }

    @Override
    public double getBaseMaxHealth(LivingEntity e) {
        return Objects.requireNonNull(e.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getBaseValue();
    }

    @Override
    public void setMaxHealth(LivingEntity e, double value) {
        AttributeInstance a = e.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (a != null) a.setBaseValue(value);
    }

    @Override
    public PotionEffectType getPotionEffect(String name) {
        return PotionUtils.find(name);
    }

    @Override
    public Enchantment findEnchantment(String name) {
        if (name == null) return null;
        String n = name.trim();
        try {
            Enchantment e = Enchantment.getByKey(NamespacedKey.minecraft(n.toLowerCase(Locale.ROOT)));
            if (e != null) return e;
        } catch (IllegalArgumentException ignored) {}
        return Enchantment.getByName(n.toUpperCase(Locale.ROOT));
    }

    @Override
    public PotionType getPotionType(String name) {
        return PotionUtils.findType(name);
    }

    @Override
    public boolean setBasePotion(PotionMeta meta, PotionType type, boolean extended, boolean upgraded) {
        return PotionUtils.setBasePotion(meta, type, extended, upgraded);
    }

    @Override
    public OfflinePlayer getOfflinePlayer(String name) {
        return Bukkit.getOfflinePlayer(name);
    }
}
