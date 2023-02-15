package tk.shanebee.hg.data;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.Inventory;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.game.Bound;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.tasks.StartChestDropTask;

public class ChestDrop {
    private Inventory chestInv;
    private Block chestBlock;
    private Location location;
    private Game game;
    private int startTaskId;
    private int curDropTaskId;
    private BlockState initBeaconBlock;

    private Material chestType;
    public ChestDrop(Game game, Material chestType) {
        this.chestType = chestType;
        this.game = game;
        chestInv = Bukkit.createInventory(null, 27, "Loot Crate");
        findLocation();
        startTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(HG.getPlugin(), new StartChestDropTask(game, this), Config.randomChestInterval);
        HG.getPlugin().getManager().fillChest(chestInv, game, true);
    }

    private void findLocation() {
        Bound bound = game.getGameArenaData().getBound();
        Integer[] i = bound.getRandomLocs();

        int x = i[0];
        int y = i[1];
        int z = i[2];
        World w = bound.getWorld();

        while (w.getBlockAt(x, y, z).getType() == Material.AIR) {
            y--;

            if (y <= 0) {
                i = bound.getRandomLocs();

                x = i[0];
                y = i[1];
                z = i[2];
            }
        }
        Block beaconBlock = w.getBlockAt(x,y,z);
        game.getGameBlockData().recordBlockPlace(beaconBlock.getState());
        initBeaconBlock = beaconBlock.getState();
        beaconBlock.setType(Material.BEACON);

        location = new Location(w, x, y+1, z);
    }

    public Inventory getChestInv() {
        return chestInv;
    }

    public Block getChestBlock() {
        return chestBlock;
    }
    public void setChestBlock(Block chestBlock) {
        this.chestBlock = chestBlock;
    }

    public Location getLocation() {
        return location;
    }
    public Material getMaterial() {
        return chestType;
    }
    public void setCurDropTaskId(int id) {
        curDropTaskId = id;
    }
    public BlockState getInitBeaconBlock() {
        return initBeaconBlock;
    }
    public void shutdown() {
        Bukkit.getScheduler().cancelTask(startTaskId);
        Bukkit.getScheduler().cancelTask(curDropTaskId);
    }
}
