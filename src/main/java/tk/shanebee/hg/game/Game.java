package tk.shanebee.hg.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import tk.shanebee.hg.HG;
import tk.shanebee.hg.Status;
import tk.shanebee.hg.data.Config;
import tk.shanebee.hg.data.Language;
import tk.shanebee.hg.data.Leaderboard;
import tk.shanebee.hg.data.PlayerData;
import tk.shanebee.hg.events.GameEndEvent;
import tk.shanebee.hg.events.GameStartEvent;
import tk.shanebee.hg.game.GameCommandData.CommandType;
import tk.shanebee.hg.managers.ChestDropManager;
import tk.shanebee.hg.managers.KitManager;
import tk.shanebee.hg.managers.MobManager;
import tk.shanebee.hg.managers.PlayerManager;
import tk.shanebee.hg.tasks.*;
import tk.shanebee.hg.tasks.TimerTask;
import tk.shanebee.hg.util.Util;
import tk.shanebee.hg.util.Vault;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.*;

/**
 * General game object
 */
@SuppressWarnings("unused")
public class Game {

    final HG plugin;
    final Language lang;

    // Managers
    KitManager kitManager;
    private final MobManager mobManager;
    private final PlayerManager playerManager;
    private ChestDropManager chestDropManager;

    // Task ID's here!
    private SpawnerTask spawner;
    private FreeRoamTask freeRoam;
    private StartingTask starting;
    private TimerTask timer;

    // Data Objects
    final GameArenaData gameArenaData;
    final GameBarData bar;
    final GamePlayerData gamePlayerData;
    final GameBlockData gameBlockData;
    final GameItemData gameItemData;
    final GameCommandData gameCommandData;
    final GameBorderData gameBorderData;

    /**
     * Create a new game
     * <p>Internally used when loading from config on server start</p>
     *
     * @param name       Name of this game
     * @param bound      Bounding region of this game
     * @param spawns     List of spawns for this game
     * @param lobbySign  Lobby sign block
     * @param timer      Length of the game (in seconds)
     * @param minPlayers Minimum players to be able to start the game
     * @param maxPlayers Maximum players that can join this game
     * @param roam       Roam time for this game
     * @param isReady    If the game is ready to start
     * @param cost       Cost of this game
     */
    public Game(String name, Bound bound, List<Location> spawns, Sign lobbySign, int timer, int minPlayers, int maxPlayers, int roam, boolean isReady, int cost) {
        this(name, bound, timer, minPlayers, maxPlayers, roam, cost);
        gameArenaData.spawns.addAll(spawns);
        this.gameBlockData.sign1 = lobbySign;

        // If lobby signs are not properly setup, game is not ready
        if (!this.gameBlockData.setLobbyBlock(lobbySign)) {
            isReady = false;
        }
        gameArenaData.setStatus(isReady ? Status.READY : Status.BROKEN);

        this.kitManager = plugin.getKitManager();
    }

    /**
     * Create a new game
     * <p>Internally used when creating a game with the <b>/hg create</b> command</p>
     *
     * @param name       Name of this game
     * @param bound      Bounding region of this game
     * @param timer      Length of the game (in seconds)
     * @param minPlayers Minimum players to be able to start the game
     * @param maxPlayers Maximum players that can join this game
     * @param roam       Roam time for this game
     * @param cost       Cost of this game
     */
    public Game(String name, Bound bound, int timer, int minPlayers, int maxPlayers, int roam, int cost) {
        this.plugin = HG.getPlugin();
        this.gameArenaData = new GameArenaData(this, name, bound, timer, minPlayers, maxPlayers, roam, cost);
        this.gameArenaData.status = Status.NOTREADY;
        this.playerManager = HG.getPlugin().getPlayerManager();
        this.lang = plugin.getLang();
        this.kitManager = plugin.getKitManager();
        this.mobManager = new MobManager(this);
        this.bar = new GameBarData(this);
        this.gamePlayerData = new GamePlayerData(this);
        this.gameBlockData = new GameBlockData(this);
        this.gameItemData = new GameItemData(this);
        this.gameCommandData = new GameCommandData(this);
        this.gameBorderData = new GameBorderData(this);
        this.gameBorderData.setBorderSize(Config.borderFinalSize);
        this.gameBorderData.setBorderTimer(Config.borderCountdownStart, Config.borderCountdownEnd);
        this.chestDropManager = new ChestDropManager(this);
    }

    public ChestDropManager getChestDropManager() {
        return chestDropManager;
    }

    /**
     * Get an instance of the GameArenaData
     *
     * @return Instance of GameArenaData
     */
    public GameArenaData getGameArenaData() {
        return gameArenaData;
    }

    /**
     * Get an instance of the GameBarData
     *
     * @return Instance of GameBarData
     */
    public GameBarData getGameBarData() {
        return bar;
    }

    /**
     * Get an instance of the GamePlayerData
     *
     * @return Instance of GamePlayerData
     */
    public GamePlayerData getGamePlayerData() {
        return gamePlayerData;
    }

    /**
     * Get an instance of the GameBlockData
     *
     * @return Instance of GameBlockData
     */
    public GameBlockData getGameBlockData() {
        return gameBlockData;
    }

    /**
     * Get an instance of the GameItemData
     *
     * @return Instance of GameItemData
     */
    public GameItemData getGameItemData() {
        return gameItemData;
    }

    /**
     * Get an instance of the GameCommandData
     *
     * @return Instance of GameCommandData
     */
    public GameCommandData getGameCommandData() {
        return gameCommandData;
    }

    /**
     * Get an instance of the GameBorderData
     *
     * @return Instance of GameBorderData
     */
    public GameBorderData getGameBorderData() {
        return gameBorderData;
    }

    public StartingTask getStartingTask() {
        return this.starting;
    }

    /**
     * Get the location of the lobby for this game
     *
     * @return Location of the lobby sign
     */
    public Location getLobbyLocation() {
        return gameBlockData.sign1.getLocation();
    }

    /**
     * Get the kits for this game
     *
     * @return The KitManager kit for this game
     */
    public KitManager getKitManager() {
        return this.kitManager;
    }

    /**
     * Set the kits for this game
     *
     * @param kit The KitManager kit to set
     */
    @SuppressWarnings("unused")
    public void setKitManager(KitManager kit) {
        this.kitManager = kit;
    }

    /**
     * Get this game's MobManager
     *
     * @return MobManager for this game
     */
    public MobManager getMobManager() {
        return this.mobManager;
    }

    /**
     * Start the pregame countdown
     */
    public void startPreGame() {
        // Call the GameStartEvent
        Bukkit.getPluginManager().callEvent(new GameStartEvent(this));
        gameArenaData.status = Status.COUNTDOWN;
        starting = new StartingTask(this);
        gameBlockData.updateLobbyBlock();
    }

    /**
     * Start the free roam state of the game
     */
    public void startFreeRoam() {
        gameArenaData.status = Status.BEGINNING;
        gameBlockData.updateLobbyBlock();
        gameArenaData.bound.removeEntities();
        freeRoam = new FreeRoamTask(this);
        gameCommandData.runCommands(CommandType.START, null);
    }

    /**
     * Start the game
     */
    public void startGame() {
        gameArenaData.status = Status.RUNNING;
        if (Config.spawnmobs) spawner = new SpawnerTask(this, Config.spawnmobsinterval);
        if (Config.randomChest) chestDropManager.startChestDrop();
        gameBlockData.updateLobbyBlock();
        if (Config.bossbar) {
            bar.createBossbar(gameArenaData.timer);
        }
        if (Config.borderEnabled && Config.borderOnStart) {
            gameBorderData.setBorder(gameArenaData.timer);
        }
        timer = new TimerTask(this, gameArenaData.timer);
    }

    public void cancelTasks() {
        if (spawner != null) spawner.stop();
        if (timer != null) timer.stop();
        if (starting != null) starting.stop();
        if (freeRoam != null) freeRoam.stop();
        if (freeRoam != null) chestDropManager.shutdown();
    }

    /**
     * Stop the game
     */
    public void stop() {
        stop(false);
    }

    public void stop(Boolean death) {
        // === FULL-SCREEN TITLE + 5s DELAY ===
        // Build winner name before we clear anything
        java.util.List<java.util.UUID> winPreview = new java.util.ArrayList<>(gamePlayerData.players);
        String winnerName = tk.shanebee.hg.util.Util.translateStop(
                tk.shanebee.hg.util.Util.convertUUIDListToStringList(winPreview)
        );

        // Send a full-screen title (not chat) to players & spectators
        String title = ChatColor.GOLD + winnerName;
        String subtitle = ChatColor.YELLOW + "is the Victor " + gameArenaData.getName() + "!";
        int fadeIn = 10, stay = 60, fadeOut = 10;

        java.util.List<Player> winnerPlayers = new java.util.ArrayList<>();
        for (java.util.UUID u : winPreview) {
            Player wp = Bukkit.getPlayer(u);
            if (wp != null) winnerPlayers.add(wp);
        }

        for (java.util.UUID uuid : gamePlayerData.getPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 0.8f);
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f);
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        }
        for (java.util.UUID uuid : gamePlayerData.getSpectators()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 0.8f);
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f);
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        }

        launchWinnerFireworks(winnerPlayers);

        // 5secs = 100 ticks
        int POST_GAME_DELAY_TICKS = 200;
        if (plugin.isEnabled()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> doStop(death), POST_GAME_DELAY_TICKS);
            return; // prevent running the original body now
        }
        doStop(death);
    }
    /**
     * Stop the game
     *
     * @param death Whether the game stopped after the result of a death (false = no winnings payed out)
     */
    public void doStop(Boolean death) {
        if (Config.borderEnabled) {
            gameBorderData.resetBorder();
        }
        gameArenaData.bound.removeEntities();
        List<UUID> win = new ArrayList<>();
        cancelTasks();
        for (UUID uuid : gamePlayerData.players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                PlayerData playerData = playerManager.getPlayerData(uuid);
                assert playerData != null;
                Location previousLocation = playerData.getPreviousLocation();

                gamePlayerData.heal(player);
                playerData.restore(player);
                win.add(uuid);
                gamePlayerData.exit(player, previousLocation);
                playerManager.removePlayerData(uuid);
            }
        }

        for (UUID uuid : gamePlayerData.getSpectators()) {
            Player spectator = Bukkit.getPlayer(uuid);
            if (spectator != null) {
                gamePlayerData.leaveSpectate(spectator);
            }
        }

        if (gameArenaData.status == Status.RUNNING) {
            bar.clearBar();
        }

        if (!win.isEmpty() && death) {
            double db = (double) Config.cash / win.size();
            for (UUID u : win) {
                if (Config.giveReward) {
                    Player p = Bukkit.getPlayer(u);
                    assert p != null;
                    if (!Config.rewardCommands.isEmpty()) {
                        for (String cmd : Config.rewardCommands) {
                            if (!cmd.equalsIgnoreCase("none"))
                                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("<player>", p.getName()));
                        }
                    }
                    if (!Config.rewardMessages.isEmpty()) {
                        for (String msg : Config.rewardMessages) {
                            if (!msg.equalsIgnoreCase("none"))
                                Util.scm(p, msg.replace("<player>", p.getName()));
                        }
                    }
                    if (Config.cash != 0) {
                        Vault.economy.depositPlayer(Bukkit.getServer().getOfflinePlayer(u), db);
                        Util.scm(p, lang.winning_amount.replace("<amount>", String.valueOf(db)));
                    }
                }
                plugin.getLeaderboard().addStat(u, Leaderboard.Stats.WINS);
                plugin.getLeaderboard().addStat(u, Leaderboard.Stats.GAMES);
            }
        }
        gameBlockData.clearChests();
        String winner = Util.translateStop(Util.convertUUIDListToStringList(win));

        // Broadcast wins
        if (death) {
            String broadcast = lang.player_won.replace("<arena>", gameArenaData.name).replace("<winner>", winner);
            if (Config.broadcastWinMessages) {
                Util.broadcast(broadcast);
            } else {
                gamePlayerData.msgAllPlayers(broadcast);
            }
        }
        if (gameBlockData.requiresRollback()) {
            if (plugin.isEnabled()) {
                new Rollback(this);
            } else {
                // Force rollback if server is stopping
                gameBlockData.forceRollback();
            }
        } else {
            gameArenaData.status = Status.READY;
            gameBlockData.updateLobbyBlock();
        }
        gameArenaData.updateBoards();
        gameCommandData.runCommands(CommandType.STOP, null);

        // Call GameEndEvent
        Collection<Player> winners = new ArrayList<>();
        for (UUID uuid : win) {
            winners.add(Bukkit.getPlayer(uuid));
        }

        // Game has ended, we can clear all players now
        gamePlayerData.clearPlayers();
        gamePlayerData.clearSpectators();
        gamePlayerData.clearTeams();
        Bukkit.getPluginManager().callEvent(new GameEndEvent(this, winners, death));
    }

    void updateAfterDeath(Player player, boolean death) {

        // strike lightning effect at death location
        if (player != null && player.getLocation().getWorld() != null) {
            player.getWorld().strikeLightningEffect(player.getLocation());
        }

        Status status = gameArenaData.status;
        if (status == Status.RUNNING || status == Status.BEGINNING || status == Status.COUNTDOWN) {
            if (isGameOver()) {
                if (!death) {
                    for (UUID uuid : gamePlayerData.players) {
                        if (gamePlayerData.kills.get(Bukkit.getPlayer(uuid)) >= 1) {
                            death = true;
                        }
                    }
                }
                boolean finalDeath = death;
                if (plugin.isEnabled()) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        stop(finalDeath);
                        gameBlockData.updateLobbyBlock();
                        gameArenaData.updateBoards();
                    }, 20);
                } else {
                    stop(finalDeath);
                }

            }
        } else if (status == Status.WAITING) {
            gamePlayerData.msgAll(lang.player_left_game
                    .replace("<arena>", gameArenaData.getName())
                    .replace("<player>", player.getName()) +
                    (gameArenaData.minPlayers - gamePlayerData.players.size() <= 0 ? "!" : ": " + lang.players_to_start
                            .replace("<amount>", String.valueOf((gameArenaData.minPlayers - gamePlayerData.players.size())))));
        }
        gameBlockData.updateLobbyBlock();
        gameArenaData.updateBoards();
    }

    boolean isGameOver() {
        if (gamePlayerData.players.size() <= 1) return true;
        for (UUID uuid : gamePlayerData.players) {
            Team team = Objects.requireNonNull(playerManager.getPlayerData(uuid)).getTeam();

            if (team != null && (team.getPlayers().size() >= gamePlayerData.players.size())) {
                for (UUID u : gamePlayerData.players) {
                    if (!team.getPlayers().contains(u)) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private void launchWinnerFireworks(java.util.Collection<Player> winners) {
        if (winners == null || winners.isEmpty()) return;

        final int bursts = 6;           // how many total spawns
        final int intervalTicks = 10;   // every 0.5s
        final java.util.Random rng = new java.util.Random();

        for (Player winner : winners) {
            if (winner == null || winner.getWorld() == null) continue;
            final World world = winner.getWorld();
            final Location base = winner.getLocation().clone().add(0, 1, 0);

            // schedule a short series of launches at this winner's spot
            for (int i = 0; i < bursts; i++) {
                final int delay = i * intervalTicks;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Firework fw = (Firework) world.spawn(base, Firework.class);
                    FireworkMeta meta = fw.getFireworkMeta();

                    // random style/colors each time
                    FireworkEffect.Type type;
                    switch (rng.nextInt(4)) {
                        case 0:
                            type = FireworkEffect.Type.BALL;
                            break;
                        case 1:
                            type = FireworkEffect.Type.BALL_LARGE;
                            break;
                        case 2:
                            type = FireworkEffect.Type.BURST;
                            break;
                        default:
                            type = FireworkEffect.Type.STAR;
                            break;
                    }

                    Color c1 = randomNiceColor(rng);
                    Color c2 = randomNiceColor(rng);
                    boolean flicker = rng.nextBoolean();
                    boolean trail = rng.nextBoolean();

                    meta.addEffect(FireworkEffect.builder()
                            .with(type)
                            .withColor(c1)
                            .withFade(c2)
                            .flicker(flicker)
                            .trail(trail)
                            .build());
                    meta.setPower(1 + rng.nextInt(2)); // 1–2 height
                    fw.setFireworkMeta(meta);
                }, delay);
            }
        }
    }

    private Color randomNiceColor(java.util.Random rng) {
        // a curated palette that looks good
        Color[] palette = new Color[] {
                Color.RED, Color.AQUA, Color.BLUE, Color.LIME, Color.FUCHSIA,
                Color.ORANGE, Color.SILVER, Color.PURPLE, Color.YELLOW, Color.WHITE
        };
        return palette[rng.nextInt(palette.length)];
    }

    @Override
    public String toString() {
        return "Game{name='" + gameArenaData.name + '\'' + ", bound=" + gameArenaData.bound + '}';
    }

}
