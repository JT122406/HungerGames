package tk.shanebee.adapters;

import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public interface VersionAdapter {
    BlockState getBlockState(Block block);
    BlockState getBlockSnapshot(Block block);
    String getWorldId(World world);
    World findWorld(String id);
    double getMaxHealth(LivingEntity entity);
    double getBaseMaxHealth(LivingEntity entity);
    void setMaxHealth(LivingEntity entity, double value);
    Enchantment findEnchantment(String name);
    PotionEffectType getPotionEffect(String name);
    PotionType getPotionType(String name);
    boolean setBasePotion(PotionMeta meta, PotionType type, boolean extended, boolean upgraded);
    OfflinePlayer getOfflinePlayer(String name);
}
