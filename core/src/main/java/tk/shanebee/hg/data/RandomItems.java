package tk.shanebee.hg.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.util.Util;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Handler for random items
 */
public class RandomItems {

    private FileConfiguration item = null;
    private File customConfigFile = null;
    public int size = 0;
    private final HG plugin;

    public RandomItems(HG plugin) {
        this.plugin = plugin;
        reloadCustomConfig();
        Util.log("Loading random items...");
        load();
    }

    private void reloadCustomConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder(), "items.yml");
        }
        if (!customConfigFile.exists()) {
            plugin.saveResource("items.yml", false);
            Util.log("New items.yml file has been &asuccessfully generated!");
        }
        item = YamlConfiguration.loadConfiguration(customConfigFile);
    }

    public void load() {
        loadItems(item.getStringList("items"), plugin.getItemCostMap(), plugin.getItemRarityMap());
        loadItems(item.getStringList("bonus"), plugin.getBonusCostMap(), plugin.getBonusRarityMap());
        Util.log(plugin.getItemRarityMap().keySet().size() + " Random items have been &aloaded!");
        Util.log(plugin.getBonusRarityMap().keySet().size() + " Random bonus items have been &aloaded!");
    }

    void loadItems(List<String> itemDefinitions, Map<ItemStack, Integer> costMap, Map<ItemStack, Integer> rarityMap) {
        for (String s : itemDefinitions) {
            int cost = 1;
            int rarity = 1;
            if (s.contains("cost:") && s.contains("rarity:")) {
                int costStartIndex = s.indexOf("cost:") + 5;
                int costEndIndex = s.indexOf(' ', costStartIndex) == -1 ? s.length() : s.indexOf(' ', costStartIndex);
                String costStr = s.substring(costStartIndex, costEndIndex);
                cost = Integer.parseInt(costStr);
                int rarityStartIndex = s.indexOf("rarity:") + 7;
                int rarityEndIndex = s.indexOf(' ', rarityStartIndex) == -1 ? s.length() : s.indexOf(' ', rarityStartIndex);
                String rarityStr = s.substring(rarityStartIndex, rarityEndIndex);
                rarity = Integer.parseInt(rarityStr);
            }
            else if (s.contains("cost:")) {
                Util.log("Item definition %s doesn't contain a rarity! Defaulting to 1...", s);
                int costStartIndex = s.indexOf("cost:") + 5;
                int costEndIndex = s.indexOf(' ', costStartIndex) == -1 ? s.length() : s.indexOf(' ', costStartIndex);
                String costStr = s.substring(costStartIndex, costEndIndex);
                cost = Integer.parseInt(costStr);
            }
            else if (s.contains("rarity:")) {
                Util.log("Item definition %s doesn't contain a cost! Defaulting to 1...", s);
                int rarityStartIndex = s.indexOf("rarity:") + 7;
                int rarityEndIndex = s.indexOf(' ', rarityStartIndex) == -1 ? s.length() : s.indexOf(' ', rarityStartIndex);
                String rarityStr = s.substring(rarityStartIndex, rarityEndIndex);
                rarity = Integer.parseInt(rarityStr);
            }
            else {
                Util.log("Item definition %s doesn't contain a cost or a rarity! Defaulting to 1...", s);
            }
            String itemStackStr = s.replaceAll("cost:", "").replaceAll("rarity:", "");
            ItemStack readItem = plugin.getItemStackManager().getItem(itemStackStr, true);
            costMap.put(readItem, cost);
            rarityMap.put(readItem, rarity);
        }
    }

}
