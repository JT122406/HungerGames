package tk.shanebee.hg.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.Language;
import tk.shanebee.hg.game.Bound;
import tk.shanebee.hg.game.Game;
import tk.shanebee.hg.game.GameArenaData;
import tk.shanebee.hg.game.GameItemData;
import tk.shanebee.hg.util.Util;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * General manager for games
 */
public class Manager {

	private final HG plugin;
	private final Language lang;
	private final ThreadLocalRandom rg = ThreadLocalRandom.current();

	public Manager(HG plugin) {
		this.plugin = plugin;
		this.lang = plugin.getLang();
	}

    /** Run arena debugger
     * @param sender Sender who issued this debuger
     * @param gameName Name of the game to debug
     */
	public void runDebugger(CommandSender sender, String gameName) {
		Configuration arenadat = HG.getPlugin().getArenaConfig().getCustomConfig();
		boolean isReady = true;
		List<Location> spawns = new ArrayList<>();
		Sign lobbysign = null;
		int timer = 0;
		int minplayers = 0;
		int maxplayers = 0;
		boolean border = Config.borderEnabled;
		Location borderCenter = null;
		int borderSize = 0;
		int borderCountdownStart = 0;
		int borderCountdownEnd = 0;
		int chestRefill = 0;
		World world = null;

		try {
			timer = arenadat.getInt("arenas." + gameName + ".info." + "timer");
			minplayers = arenadat.getInt("arenas." + gameName + ".info." + "min-players");
			maxplayers = arenadat.getInt("arenas." + gameName + ".info." + "max-players");

			if (arenadat.isSet("arenas." + gameName + ".border.center")) {
				borderCenter = HG.getPlugin().getArenaConfig().getSLoc(Objects.requireNonNull(arenadat.getString("arenas." + gameName + ".border.center")));
			}
			if (arenadat.isSet("arenas." + gameName + ".border.size")) {
				borderSize = arenadat.getInt("arenas." + gameName + ".border.size");
			} else {
				borderSize = Config.borderFinalSize;
			}
			if (arenadat.isSet("arenas." + gameName + ".border.countdown-start") &&
					arenadat.isSet("arenas." + gameName + ".border.countdown-end")) {
				borderCountdownStart = arenadat.getInt("arenas." + gameName + ".border.countdown-start");
				borderCountdownEnd = arenadat.getInt("arenas." + gameName + ".border.countdown-end");
			} else {
				borderCountdownStart = Config.borderCountdownStart;
				borderCountdownEnd = Config.borderCountdownEnd;
			}
			if (arenadat.isSet("arenas." + gameName + ".chest-refill")) {
				chestRefill = arenadat.getInt("arenas." + gameName + ".chest-refill");
			}
		} catch (Exception e) {
			Util.scm(sender, "&cUnable to load information for arena " + gameName + "!");
			isReady = false;
		}

		try {
			lobbysign = (Sign) HG.getPlugin().getArenaConfig().getSLoc(Objects.requireNonNull(arenadat.getString("arenas." + gameName + "." + "lobbysign"))).getBlock().getState();
		} catch (Exception e) {
			Util.scm(sender, "&cUnable to load lobbysign for arena " + gameName + "!");
			isReady = false;
		}

		try {
			for (String l : arenadat.getStringList("arenas." + gameName + "." + "spawns")) {
				spawns.add(HG.getPlugin().getArenaConfig().getLocFromString(l));
			}
			int count = arenadat.getStringList("arenas." + gameName + "." + "spawns").size();
			if (count < maxplayers) {
				Util.scm(sender, "&cYou need to add " + (maxplayers - count) + " more spawns!");
				isReady = false;
			}
		} catch (Exception e) {
			Util.scm(sender, "&cUnable to load random spawns for arena " + gameName + "!");
			isReady = false;
		}

		try {
			@SuppressWarnings("unused")
            Bound b = new Bound(arenadat.getString("arenas." + gameName + ".bound." + "world"), HG.getPlugin().getArenaConfig().BC(gameName, "x"), HG.getPlugin().getArenaConfig().BC(gameName, "y"), HG.getPlugin().getArenaConfig().BC(gameName, "z"), HG.getPlugin().getArenaConfig().BC(gameName, "x2"), HG.getPlugin().getArenaConfig().BC(gameName, "y2"), HG.getPlugin().getArenaConfig().BC(gameName, "z2"));
			world = b.getWorld();
		} catch (Exception e) {
			Util.scm(sender, "&cUnable to load region bounds for arena " + gameName + "!");
			isReady = false;
		}

		if (world == null) {
			Util.scm(sender, "&cThe world for this arena does not exist!");
			isReady = false;
		}

		if (isReady) {
			Util.scm(sender,"&7&l---= &3&lYour HungerGames arena is ready to run! &7&l=---");
			Util.scm(sender, "&7Spawns:&b " + spawns.size());
			Util.scm(sender, "&7Lobby:&b x:" + lobbysign.getX() +", y:"+ lobbysign.getY() +", z:"+ lobbysign.getZ());
			Util.scm(sender, "&7Timer:&b " + timer);
			Util.scm(sender, "&7MinPlayers:&b " + minplayers);
			Util.scm(sender, "&7MaxPlayers:&b " + maxplayers);
			if (chestRefill > 0)
				Util.scm(sender, "&7Chest Refill: &b" + chestRefill + " seconds");
			if (border) {
				Util.scm(sender, "&7Border: &aEnabled");
				if (borderCenter != null)
					Util.scm(sender, "&7Border Center: &bx:" + borderCenter.getX() + ", y:" + borderCenter.getY() + ", z:" + borderCenter.getZ());
				if (borderSize > 0)
					Util.scm(sender, "&7Border Size: &b" + borderSize);
				if (borderCountdownStart > 0) {
					Util.scm(sender, "&7Border Timer Start: &b" + borderCountdownStart + " seconds");
					Util.scm(sender, "&7Border Timer End: &b" + borderCountdownEnd + " seconds");
				}
			} else {
				Util.scm(sender, "&7Border: &cDisabled");
			}
		}
	}

    /** Check the status of a game while being set up
     * @param game Game to check
     * @param player Player issuing the check
     */
	public void checkGame(Game game, Player player) {
		GameArenaData gameArenaData = game.getGameArenaData();
		String name = gameArenaData.getName();
		if (gameArenaData.getSpawns().size() <  gameArenaData.getMaxPlayers()) {
			Util.sendPrefixedMessage(player, lang.check_need_more_spawns.replace("<number>",
					String.valueOf(gameArenaData.getMaxPlayers() - gameArenaData.getSpawns().size())));
		} else if (gameArenaData.getStatus() == Status.BROKEN) {
			Util.sendPrefixedMessage(player, lang.check_broken_debug.replace("<arena>", name));
			Util.sendPrefixedMessage(player, lang.check_broken_debug_2.replace("<arena>", name));
		} else if (!game.getGameBlockData().isLobbyValid()) {
			Util.sendPrefixedMessage(player, lang.check_invalid_lobby);
			Util.sendPrefixedMessage(player, lang.check_set_lobby.replace("<arena>", name));
		} else {
			Util.sendPrefixedMessage(player, lang.check_ready_run.replace("<arena>", name));
			gameArenaData.setStatus(Status.READY);
		}
	}

    /** Fill chests in a game
     * @param inv Inventory to fill
     * @param game Game this chest is in
     * @param bonus Whether or not this is a bonus chest
     */
	public void fillChest(Inventory inv, Game game, boolean bonus) {
		GameItemData gameItemData = game.getGameItemData();
		inv.clear();

		Map<ItemStack, Integer> itemRarityMap =  bonus ? gameItemData.getBonusRarityMap() : gameItemData.getItemRarityMap();
		Map<ItemStack, Integer> itemCostMap =  bonus ? gameItemData.getBonusCostMap() : gameItemData.getItemCostMap();
		int maxCost = bonus ? Config.maxbonuscontent : Config.maxchestcontent;
		int minCost = bonus ? Config.minbonuscontent : Config.minchestcontent;

		ArrayList<ItemStack> itemList = new ArrayList<>();
		for (Map.Entry<ItemStack, Integer> e : itemRarityMap.entrySet()) {
			for (int i = 0; i < e.getValue(); i++){
				itemList.add(e.getKey());
			}
		}
		Collections.shuffle(itemList);

		int costGoal = rg.nextInt(minCost, maxCost + 1);
		int chestCost = 0;
		ArrayList<ItemStack> chestContents = new ArrayList<>();
		int numTries = 0;
		while (chestCost < costGoal && numTries < 10) {
			int nextItemIndex = rg.nextInt(itemList.size());
			ItemStack nextItem = itemList.get(nextItemIndex);
			int nextItemCost = itemCostMap.get(nextItem);
			if (nextItemCost <= (costGoal - chestCost) && !chestContents.contains(nextItem)) {
				chestCost += nextItemCost;
				chestContents.add(nextItem);
				numTries = 0;
			}
			else {
				numTries++;
			}
		}
		HashSet<Integer> pickedSlots = new HashSet<>();
		chestContents.forEach((itemStack -> {
			int slot = rg.nextInt(inv.getSize());
			while (pickedSlots.contains(slot)) {
				slot = rg.nextInt(inv.getSize());
			}
			pickedSlots.add(slot);
			inv.setItem(slot, itemStack);
		}));

	}

	/** Check if a location is in a game's bounds
	 * @param location The location to check for a game
	 * @return True if the location is within a game's bounds
	 */
	public boolean isInRegion(Location location) {
		for (Game g : plugin.getGames()) {
			if (g.getGameArenaData().isInRegion(location))
				return true;
		}
		return false;
	}

	/** Get a game at a location
	 * @param location The location to check for a game
	 * @return The game
	 */
	public Game getGame(Location location) {
		for (Game g : plugin.getGames()) {
			if (g.getGameArenaData().isInRegion(location))
				return g;
		}
		return null;
	}

	/** Get a game by name
	 * @param name The name of the game to find
	 * @return The game
	 */
	public Game getGame(String name) {
		for (Game g : plugin.getGames()) {
			if (g.getGameArenaData().getName().equalsIgnoreCase(name)) {
				return g;
			}
		}
		return null;
	}

    /** Get the number of games running
     * @return Number of games running
     */
    public int gamesRunning() {
        int i = 0;
        for (Game game : plugin.getGames()) {
            switch (game.getGameArenaData().getStatus()) {
                case RUNNING:
                case COUNTDOWN:
                case BEGINNING:
                case ROLLBACK:
                    i++;
            }
        }
        return i;
    }

}
