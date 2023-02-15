package tk.shanebee.hg.tasks;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.data.ChestDrop;
import tk.shanebee.hg.game.Game;

public class ChestDropTask implements Runnable {

    private final Game game;
    private StartChestDropTask parentTask;
    private ChestDrop chestDrop;
    private Block prevBlock;
    private int blocksToGo;

    public ChestDropTask(Game game, ChestDrop chestDrop, Block prevBlock, int blocksToGo) {
        this.game = game;
        this.chestDrop = chestDrop;
        this.prevBlock = prevBlock;
        this.blocksToGo = blocksToGo;
    }

    public void run() {
        blocksToGo--;
        World w = game.getGameArenaData().getBound().getWorld();
        Location blockLoc = prevBlock.getLocation();
        Location newBlockLoc = blockLoc.subtract(0, 1, 0);
        if (prevBlock.getType() != Material.AIR) {
            prevBlock.setType(Material.AIR);
            game.getGameBlockData().recordBlockPlace(prevBlock.getState());
            w.playSound(blockLoc, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, SoundCategory.NEUTRAL, 1f, 1f);
        }
        Block newBlock = w.getBlockAt(newBlockLoc);
        newBlock.setType(chestDrop.getMaterial());
        chestDrop.setChestBlock(newBlock);
        if (blocksToGo > 0) {
            int newTaskId = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(HG.getPlugin(), new ChestDropTask(game, chestDrop, newBlock, blocksToGo), 20);
            chestDrop.setCurDropTaskId(newTaskId);
        }
        else {
            w.spawnEntity(blockLoc, EntityType.FIREWORK, true);
            w.spawnEntity(blockLoc, EntityType.FIREWORK, true);
            w.spawnEntity(blockLoc, EntityType.FIREWORK, true);
            BlockState origBlockState = chestDrop.getInitBeaconBlock();
            Block curBeaconBlock = origBlockState.getWorld().getBlockAt(origBlockState.getLocation());
            curBeaconBlock.setBlockData(origBlockState.getBlockData());
            game.getChestDropManager().startChestDrop();
        }
    }

}
