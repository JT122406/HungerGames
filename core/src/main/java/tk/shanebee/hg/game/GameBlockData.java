package tk.shanebee.hg.game;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.InventoryHolder;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.ItemFrameData;
import tk.shanebee.hg.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Data class for holding a {@link Game Game's} blocks
 */
public class GameBlockData extends Data {

    private final Set<Location> chests = new HashSet<>();
    private final Set<Location> playerChests = new HashSet<>();
    private final List<BlockState> blocks = new ArrayList<>();
    private final List<ItemFrameData> itemFrameData = new ArrayList<>();
    private final Set<Location> editedSigns = new HashSet<>();

    // LobbySign
    Sign sign1;
    private Sign sign2;
    private Sign sign3;

    protected GameBlockData(Game game) {
        super(game);
    }

    /**
     * Force a rollback for this game
     * <p>This is not recommended to use as it forces all blocks to
     * rollback at once, which can cause heavy amounts of lag.</p>
     */
    public void forceRollback() {
        Collections.reverse(blocks);
        for (BlockState state : blocks) {
            state.update(true);
        }
    }

    boolean requiresRollback() {
        return !blocks.isEmpty() || !itemFrameData.isEmpty();
    }

    /**
     * Refill chests in this game
     */
    public void refillChests() {
        this.chests.clear();
    }

    void clearChests() {
        for (Location loc : chests) {
            BlockState state = HG.getPlugin().getAdapter().getBlockState(loc.getBlock());
            if (state instanceof InventoryHolder) {
                ((InventoryHolder) state).getInventory().clear();
                state.update();
            }
        }
        chests.clear();
    }

    private void addState(BlockState s) {
        if (s.getType() != Material.AIR) {
            blocks.add(s);
        }
    }

    /**
     * Add a game chest location to the game
     *
     * @param location Location of the chest to add (Needs to actually be a chest there)
     */
    public void addGameChest(Location location) {
        chests.add(location);
    }

    /**
     * Add a player placed chest to the game
     *
     * @param location Location of the chest
     */
    public void addPlayerChest(Location location) {
        playerChests.add(location);
    }

    /**
     * Check if chest at this location is logged
     *
     * @param location Location of chest to check
     * @return True if this chest was added already
     */
    public boolean isLoggedChest(Location location) {
        return chests.contains(location) || playerChests.contains(location);
    }

    /**
     * Remove a game chest from the game
     *
     * @param location Location of the chest to remove
     */
    public void removeGameChest(Location location) {
        chests.remove(location);
    }

    /**
     * Remove a player placed chest from the game
     *
     * @param location Location of the chest
     */
    public void removePlayerChest(Location location) {
        playerChests.remove(location);
    }

    /**
     * Record a block as broken in the arena to be restored when the game finishes
     *
     * @param block The block that was broken
     */
    public void recordBlockBreak(Block block) {
        Block top = block.getRelative(BlockFace.UP);

        if (!top.getType().isSolid() || !top.getType().isBlock()) {
            addState(HG.getPlugin().getAdapter().getBlockSnapshot(block.getRelative(BlockFace.UP)));
        }

        for (BlockFace bf : Util.faces) {
            Block rel = block.getRelative(bf);

            if (Util.isAttached(block, rel)) {
                addState(HG.getPlugin().getAdapter().getBlockSnapshot(rel));
            }
        }
        addState(HG.getPlugin().getAdapter().getBlockSnapshot(block));
    }

    /**
     * Add a block to be restored when the game finishes
     *
     * @param blockState BlockState to be added to the list
     */
    public void recordBlockPlace(BlockState blockState) {
        blocks.add(blockState);
    }

    /**
     * Record a sign as edited in the arena to be restored when the game finishes
     * <p>Only the first edit of each sign is recorded, as that holds its original state.
     * Must be called before the edit is applied.</p>
     *
     * @param block The sign block that is about to be edited
     */
    public void recordSignEdit(Block block) {
        if (editedSigns.add(block.getLocation())) blocks.add(HG.getPlugin().getAdapter().getBlockSnapshot(block));
    }

    /**
     * Add an item frame to be restored when the game finishes
     *
     * @param itemFrame ItemFrame to be added to the list
     */
    public void recordItemFrame(ItemFrame itemFrame) {
        itemFrameData.add(new ItemFrameData(itemFrame));
    }

    public List<ItemFrameData> getItemFrameData() {
        return itemFrameData;
    }

    /**
     * Get a list of all recorded blocks
     *
     * @return List of all recorded blocks
     */
    public List<BlockState> getBlocks() {
         Collections.reverse(blocks);
        return blocks;
    }

    /**
     * Clear the current block list
     */
    public void resetBlocks() {
        this.blocks.clear();
        this.editedSigns.clear();
    }

    /**
     * Clear the current item frame list
     */
    public void resetItemFrames() {
        this.itemFrameData.clear();
    }

    void updateLobbyBlock() {
        if (sign2 == null || sign3 == null) return;
        sign2.setLine(1, game.gameArenaData.status.getName());
        sign3.setLine(1, ChatColor.BOLD + String.valueOf(game.getGamePlayerData().players.size()) + "/" + game.gameArenaData.maxPlayers);
        sign2.update(true);
        sign3.update(true);
    }

    /**
     * Set the lobby block for this game
     *
     * @param sign The sign to which the lobby will be set at
     * @return True if lobby is set
     */
    public boolean setLobbyBlock(Sign sign) {
        try {
            this.sign1 = sign;
            Block c = sign1.getBlock();
            BlockFace face = Util.getSignFace(((Directional) sign1.getBlockData()).getFacing());
            this.sign2 = (Sign) HG.getPlugin().getAdapter().getBlockState(c.getRelative(face));
            this.sign3 = (Sign) HG.getPlugin().getAdapter().getBlockState(sign2.getBlock().getRelative(face));

            sign1.setLine(0, Util.getColString(lang.lobby_sign_1_1));
            sign1.setLine(1, Util.getColString("&l" + game.gameArenaData.name));
            sign1.setLine(2, Util.getColString(lang.lobby_sign_1_3));
            if (game.gameArenaData.cost > 0)
                sign1.setLine(3, Util.getColString(HG.getPlugin().getLang().lobby_sign_cost.replace("<cost>", String.valueOf(game.gameArenaData.cost))));
            sign2.setLine(0, Util.getColString(lang.lobby_sign_2_1));
            sign2.setLine(1, Util.getColString(game.gameArenaData.status.getName()));
            sign3.setLine(0, Util.getColString(lang.lobby_sign_3_1));
            sign3.setLine(1, Util.getColString("&l" + 0 + "/" + game.gameArenaData.maxPlayers));
            sign1.update(true);
            sign2.update(true);
            sign3.update(true);
        } catch (Exception e) {
            Util.warning("Failed to setup lobby wall for arena '%s'", game.gameArenaData.name);
            return false;
        }
        return true;
    }

    public boolean isLobbyValid() {
        try {
            return sign1 != null && sign2 != null && sign3 != null;
        } catch (Exception e) {
            return false;
        }
    }

}
