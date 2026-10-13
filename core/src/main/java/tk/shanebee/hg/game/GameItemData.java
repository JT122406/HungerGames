package tk.shanebee.hg.game;

import org.bukkit.inventory.ItemStack;
import tk.shanebee.hg.HG;

import java.util.Map;

/**
 * Data class for holding a {@link Game Game's} items
 */
@SuppressWarnings("unused")
public class GameItemData extends Data {
    private Map<ItemStack, Integer> itemRarityMap;

    private Map<ItemStack, Integer> itemCostMap;

    private Map<ItemStack, Integer> bonusRarityMap;

    private Map<ItemStack, Integer> bonusCostMap;

    protected GameItemData(Game game) {
        super(game);
        // Set default items from items.yml (if arenas.yml has items it will override this)
        this.itemCostMap = game.plugin.getItemCostMap();
        this.itemRarityMap = game.plugin.getItemRarityMap();
        this.bonusCostMap = game.plugin.getBonusCostMap();
        this.bonusRarityMap = game.plugin.getBonusRarityMap();
    }

    /**
     * Add an item to the items map for this game
     *
     * @param item ItemStack to add
     */
    public void addToItems(ItemStack item, int cost, int rarity) {
        this.itemRarityMap.put(item, rarity);
        this.itemCostMap.put(item, rarity);
    }

    /**
     * Clear the items for this game
     */
    public void clearItems() {
        this.itemRarityMap.clear();
        this.itemCostMap.clear();
    }

    /**
     * Reset the items for this game to the plugin's default items list
     */
    public void resetItemsDefault() {
        this.itemCostMap = HG.getPlugin().getItemCostMap();
        this.itemRarityMap = HG.getPlugin().getItemRarityMap();
    }


    /**
     * Add an item to this game's bonus items
     *
     * @param item ItemStack to add to bonus items
     */
    public void addToBonusItems(ItemStack item, int cost, int rarity) {
        this.bonusRarityMap.put(item, rarity);
        this.bonusCostMap.put(item, cost);
    }

    /**
     * Clear this game's bonus items
     */
    public void clearBonusItems() {
        this.bonusCostMap.clear();
        this.bonusRarityMap.clear();
    }

    public Map<ItemStack, Integer> getItemRarityMap() {
        return itemRarityMap;
    }

    public void setItemRarityMap(Map<ItemStack, Integer> itemRarityMap) {
        this.itemRarityMap = itemRarityMap;
    }

    public Map<ItemStack, Integer> getItemCostMap() {
        return itemCostMap;
    }

    public void setItemCostMap(Map<ItemStack, Integer> itemCostMap) {
        this.itemCostMap = itemCostMap;
    }

    public Map<ItemStack, Integer> getBonusRarityMap() {
        return bonusRarityMap;
    }

    public void setBonusRarityMap(Map<ItemStack, Integer> bonusRarityMap) {
        this.bonusRarityMap = bonusRarityMap;
    }

    public Map<ItemStack, Integer> getBonusCostMap() {
        return bonusCostMap;
    }

    public void setBonusCostMap(Map<ItemStack, Integer> bonusCostMap) {
        this.bonusCostMap = bonusCostMap;
    }

    /**
     * Reset the bonus items for this game to the plugin's default bonus items list
     */
    public void resetBonusItemsDefault() {
        this.bonusCostMap = HG.getPlugin().getBonusCostMap();
        this.bonusRarityMap = HG.getPlugin().getBonusRarityMap();
    }

}
