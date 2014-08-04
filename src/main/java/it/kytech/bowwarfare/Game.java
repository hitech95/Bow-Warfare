package it.kytech.bowwarfare;

import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.SettingsManager;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.LobbyManager;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;
import it.kytech.bowwarfare.api.PlayerJoinArenaEvent;
import it.kytech.bowwarfare.api.PlayerKilledEvent;
import it.kytech.bowwarfare.api.PlayerLeaveArenaEvent;
import it.kytech.bowwarfare.gametype.type.FreeForAll;
import it.kytech.bowwarfare.gametype.IGametype;
import it.kytech.bowwarfare.gametype.type.LastManStanding;
import it.kytech.bowwarfare.gametype.type.TeamDeathMatch;
import it.kytech.bowwarfare.logging.QueueManager;
import it.kytech.bowwarfare.manager.StatsManager;
import it.kytech.bowwarfare.manager.EconomyManager;
import it.kytech.bowwarfare.util.ItemReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import it.kytech.bowwarfare.util.bossbar.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.ScoreboardManager;

//Data container for a game
public class Game {

    public static enum GameState {

        DISABLED, LOADING, INACTIVE, WAITING,
        STARTING, INGAME, FINISHING, RESETING, ERROR
    }

    private Arena arena;
    private int gameID;
    private int gametype = -1;

    private boolean disabled = false;

    private long startTime = 0;

    private GameState state = GameState.DISABLED;

    private ArrayList<Player> activePlayers = new ArrayList<Player>();
    private ArrayList<Player> inactivePlayers = new ArrayList<Player>();
    private ArrayList<Player> spectators = new ArrayList<Player>();
    private ArrayList<Player> queue = new ArrayList<Player>();

    private ArrayList<Integer> tasks = new ArrayList<Integer>();
    private ArrayList<IGametype> availableGameTypes = new ArrayList<IGametype>();

    private HashMap<Player, Integer> nextSpectator = new HashMap< Player, Integer>();
    private HashMap<SettingsManager.OptionFlag, Object> settings = new HashMap<SettingsManager.OptionFlag, Object>();
    private HashMap<Player, ItemStack[][]> inventoryStore = new HashMap< Player, ItemStack[][]>();

    private int vote = 0;
    private ArrayList<Player> voted = new ArrayList<Player>();

    private FileConfiguration config;
    private FileConfiguration system;

    private StatsManager statMan = StatsManager.getInstance();
    private MessageManager msgmgr = MessageManager.getInstance();
    private StatsManager sm = StatsManager.getInstance();
    private ScoreboardManager sbManager = Bukkit.getScoreboardManager();

    private double rbpercent = 0;
    private String rbstatus = "";

    private int count = 20;
    private boolean countdownRunning;
    private int tid = 0;

    public Game(int gameid) {
        gameID = gameid;
        reloadConfig();
        setup();
    }

    public void reloadConfig() {
        config = SettingsManager.getInstance().getConfig();
        system = SettingsManager.getInstance().getSystemConfig();
    }

    public void $(String msg) {
        BowWarfare.$(msg);
    }

    public void debug(String msg) {
        BowWarfare.debug(msg);
    }

    public void setup() {
        state = GameState.LOADING;

        int x = system.getInt("bw-system.arenas." + gameID + ".x1");
        int y = system.getInt("bw-system.arenas." + gameID + ".y1");
        int z = system.getInt("bw-system.arenas." + gameID + ".z1");
        int x1 = system.getInt("bw-system.arenas." + gameID + ".x2");
        int y1 = system.getInt("bw-system.arenas." + gameID + ".y2");
        int z1 = system.getInt("bw-system.arenas." + gameID + ".z2");

        Location max = new Location(SettingsManager.getGameWorld(gameID), Math.max(x, x1), Math.max(y, y1), Math.max(z, z1));
        Location min = new Location(SettingsManager.getGameWorld(gameID), Math.min(x, x1), Math.min(y, y1), Math.min(z, z1));

        arena = new Arena(min, max);

        loadAvailableGameModes();

        settings = SettingsManager.getInstance().getGameSettings(gameID);

        state = GameState.WAITING;
    }

    public void reloadSettings() {
        settings = SettingsManager.getInstance().getGameSettings(gameID);
    }

    public void saveSettings() {
        SettingsManager.getInstance().saveGameSettings(settings, gameID);
    }

    public void setState(GameState s) {
        state = s;
    }

    public GameState getGameState() {
        return state;
    }

    public Arena getArena() {
        return arena;
    }

    public void addSpawn(String gamemode, Location l, String... args) {
        IGametype gameMode = getAvailableGameType(gamemode);
        if (gameMode != null) {
            gameMode.addSpawn(l, args);
        } else {
            loadAvailableGameModes();
        }
    }


    /*
     * 
     * ################################################
     * 
     * 				ENABLE
     * 
     * ################################################
     * 
     * 
     */
    public void enable() {
        state = GameState.WAITING;

        if (disabled) {
            MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gameenabled", "arena-" + gameID);
        }

        disabled = false;

        if (queue.size() > 0) {
            if (gametype < 0) {
                setAnGamemode();
            }
            int b = (availableGameTypes.get(gametype).getMaxPlayer() > queue.size()) ? queue.size() : availableGameTypes.get(gametype).getMaxPlayer();

            for (int a = 0; a < b; a++) {
                addPlayer(queue.remove(0));
            }
        }
        int c = 1;

        for (Player p : queue) {
            msgmgr.sendMessage(PrefixType.INFO, "You are now #" + c + " in line for arena " + gameID, p);
            c++;
        }

        LobbyManager.getInstance().updateWall(gameID);

        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamewaiting", "arena-" + gameID);

    }

    /*
     * 
     * ################################################
     * 
     * 				ADD PLAYER
     * 
     * ################################################
     * 
     * 
     */
    public boolean addPlayer(Player p) {
        if (SettingsManager.getInstance().getLobbySpawn() == null) {
            msgmgr.sendFMessage(PrefixType.WARNING, "error.nolobbyspawn", p);
            debug("Lobby Spawn not Set " + "bw.arena.join." + gameID);
            return false;
        }

        if (!p.hasPermission("bw.user.join." + gameID)) {
            debug("permission needed to join arena: " + "bw.arena.join." + gameID);
            msgmgr.sendFMessage(PrefixType.WARNING, "game.nopermission", p, "arena-" + gameID);
            return false;
        }

        GameManager.getInstance().removeFromOtherQueues(p, gameID);

        if (GameManager.getInstance().getPlayerGameId(p) != -1) {
            if (GameManager.getInstance().isPlayerActive(p)) {
                msgmgr.sendFMessage(PrefixType.ERROR, "error.joinmultiplegames", p);
                return false;
            }
        }

        if (p.isInsideVehicle()) {
            p.leaveVehicle();
        }

        if (spectators.contains(p)) {
            removeSpectator(p);
        }

        boolean hasAdded = false;

        if (state != GameState.DISABLED && state != GameState.RESETING && state != GameState.LOADING && state != GameState.INACTIVE && state != GameState.ERROR) {
            if (gametype < 0) {
                setAnGamemode();
            }

            if (gametype < 0) {
                msgmgr.sendFMessage(MessageManager.PrefixType.ERROR, "error.nogametype", p);
                return false;
            }

            IGametype currentG = availableGameTypes.get(gametype);
            if (currentG.getSpawnCount() == 0) {
                msgmgr.sendFMessage(MessageManager.PrefixType.ERROR, "error.nospawns", p);
                debug("Arena Without Spawns " + "bw.arena.join." + gameID);
            }

            if (activePlayers.size() < availableGameTypes.get(gametype).getMaxPlayer()) {
                PlayerJoinArenaEvent joinarena = new PlayerJoinArenaEvent(p, GameManager.getInstance().getGame(gameID));
                Bukkit.getServer().getPluginManager().callEvent(joinarena);

                if (joinarena.isCancelled()) {
                    return false;
                }

                p.setGameMode(org.bukkit.GameMode.SURVIVAL);
                p.teleport(SettingsManager.getInstance().getLobbySpawn());
                saveInv(p);
                clearInv(p);

                hasAdded = currentG.onJoin(p);
            } else {
                msgmgr.sendFMessage(PrefixType.WARNING, "error.gamefull", p, "arena-" + gameID);
                debug("Arena Full " + "bw.arena.join." + gameID);
            }
        }

        if (!hasAdded) {

            if (config.getBoolean("enable-player-queue")) {
                if (!queue.contains(p)) {
                    queue.add(p);
                    msgmgr.sendFMessage(PrefixType.INFO, "game.playerjoinqueue", p, "queuesize-" + queue.size());
                }
                int a = 1;
                for (Player qp : queue) {
                    if (qp == p) {
                        msgmgr.sendFMessage(PrefixType.INFO, "game.playercheckqueue", p, "queuepos-" + a);
                        break;
                    }
                    a++;
                }
            }

            clearInv(p);
            restoreInv(p);
        } else {

            if (getName() == null) {
                msgmgr.sendFMessage(PrefixType.INFO, "game.playerjoining", p, "arenaid-" + gameID);
            } else {
                msgmgr.sendFMessage(PrefixType.INFO, "game.playerjoiningname", p, "arenaname-" + getName());
            }

            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            activePlayers.add(p);
            sm.addPlayer(p, gameID);

            setGameInventory(p);

            LobbyManager.getInstance().updateWall(gameID);

            return true;
        }

        if (state == GameState.DISABLED) {
            msgmgr.sendFMessage(PrefixType.WARNING, "error.gamedisabled", p, "arena-" + gameID);
        } else if (state == GameState.RESETING) {
            msgmgr.sendFMessage(PrefixType.WARNING, "error.gamereseting", p);
        } else {
            msgmgr.sendFMessage(PrefixType.INFO, "error.join", p);
        }

        LobbyManager.getInstance().updateWall(gameID);
        return false;
    }

    public void vote(Player pl) {

        if (!availableGameTypes.get(gametype).requireVote()) {
            return;
        }

        if (GameState.STARTING == state) {
            msgmgr.sendMessage(PrefixType.WARNING, "Game already starting!", pl);
            return;
        }
        if (GameState.WAITING != state) {
            msgmgr.sendMessage(PrefixType.WARNING, "Game already started!", pl);
            return;
        }
        if (voted.contains(pl)) {
            msgmgr.sendMessage(PrefixType.WARNING, "You already voted!", pl);
            return;
        }
        vote++;
        voted.add(pl);
        msgmgr.sendFMessage(PrefixType.INFO, "game.playervote", pl, "player-" + pl.getName());
        if ((((vote + 0.0) / (getActivePlayers() + 0.0)) >= (config.getInt("auto-start-vote") + 0.0) / 100) && getActivePlayers() > 1) {
            countdown(config.getInt("auto-start-time"));
            for (Player p : activePlayers) {
                msgmgr.sendMessage(PrefixType.INFO, "Game starting in " + config.getInt("auto-start-time") + "!", p);
            }
        }
    }

    /*
     *
     * ################################################
     *
     * COUNTDOWN
     *
     * ################################################
     *
     *
     */
    public int getCountdownTime() {
        return count;
    }

    public void countdown(int time) {
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamestarting", "arena-" + gameID, "t-" + time);
        countdownRunning = true;
        count = time;
        Bukkit.getScheduler().cancelTask(tid);

        if (state == GameState.WAITING || state == GameState.STARTING) {
            state = GameState.STARTING;
            tid = Bukkit.getScheduler().scheduleSyncRepeatingTask((BowWarfare) GameManager.getInstance().getPlugin(), new Runnable() {
                public void run() {
                    if (count > 0) {
                        if (count % 10 == 0) {
                            msgFall(PrefixType.INFO, "game.startcountdown", "t-" + count);
                        }
                        if (count < 6) {
                            msgFall(PrefixType.INFO, "game.startcountdown", "t-" + count);

                        }
                        count--;
                        LobbyManager.getInstance().updateWall(gameID);
                    } else {
                        startGame();
                        Bukkit.getScheduler().cancelTask(tid);
                        countdownRunning = false;
                    }
                }
            }, 0, 20);

        }
    }

    /*
     * 
     * ################################################
     * 
     * 				START GAME
     * 
     * ################################################
     * 
     * 
     */
    public void startGame() {
        if (state == GameState.INGAME) {
            return;
        }

        if (activePlayers.size() <= availableGameTypes.get(gametype).getMinPlayer()) {
            for (Player pl : activePlayers) {
                msgmgr.sendFMessage(PrefixType.WARNING, "error.notenoughtplayers", pl);
                state = GameState.WAITING;
                LobbyManager.getInstance().updateWall(gameID);

            }
            return;
        } else {
            for (Player pl : activePlayers) {
                pl.setHealth(pl.getMaxHealth());
                msgmgr.sendFMessage(PrefixType.INFO, "game.goodluck", pl);
            }
        }

        state = GameState.INGAME;
        startTime = new Date().getTime();

        availableGameTypes.get(gametype).onGameStart();

        LobbyManager.getInstance().updateWall(gameID);
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamestarted", "arena-" + gameID);

    }

    /*
     * 
     * ################################################
     * 
     * 				BREAK BLOCK
     * 
     * ################################################
     * 
     * 
     */
    public boolean blockBreak(Block block, Player p) {
        return availableGameTypes.get(gametype).onBlockBreaked(block, p);
    }

    /*
     * 
     * ################################################
     * 
     * 				BREAK BLOCK
     * 
     * ################################################
     * 
     * 
     */
    public boolean blockPlace(Block block, Player p) {
        return availableGameTypes.get(gametype).onBlockPlaced(block, p);
    }

    /*
     * 
     * ################################################
     * 
     * 				INTERACT BLOCK
     * 
     * ################################################
     * 
     * 
     */
    public boolean blockInteract(Block block, Player p) {
        return availableGameTypes.get(gametype).onBlockInteract(block, p);
    }

    /*
     * 
     * ################################################
     * 
     * 				PROJECTILE HIT
     * 
     * ################################################
     * 
     * 
     */
    public boolean projectileHit(Player attacker, Projectile pro) {
        return availableGameTypes.get(gametype).onProjectileHit(attacker, pro);
    }

    /*
     * 
     * ################################################
     * 
     * 				KILL PLAYER
     * 
     * ################################################
     * 
     * 
     */
    public void killPlayer(Player p, Player killer, boolean... leave) {
        try {

            if (!activePlayers.contains(p)) {
                return;
            }

            PlayerKilledEvent pk = null;

            if (state != GameState.WAITING && p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null && leave.length < 1) {

                if (!availableGameTypes.get(gametype).onPlayerKilled(p, killer, leave.length > 0)) {
                    return;
                }

                sm.playerDied(p, gameID);

                switch (p.getLastDamageCause().getCause()) {
                    case ENTITY_ATTACK:
                        if (p.getLastDamageCause().getEntityType() == EntityType.PLAYER) {
                            msgFall(PrefixType.INFO, "death." + p.getLastDamageCause().getEntityType(),
                                    "player-" + (BowWarfare.auth.contains(p.getName()) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "") + p.getName(),
                                    "killer-" + ((killer != null) ? ((BowWarfare.auth.contains(killer.getName()) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "") + killer.getName()) : "Unknown"),
                                    "item-" + ((killer != null) ? ItemReader.getFriendlyItemName(killer.getItemInHand().getType()) : "Unknown Item"));
                            if (killer != null && p != null && activePlayers.contains(killer)) {
                                sm.addKill(killer, gameID);
                            }
                            pk = new PlayerKilledEvent(p, this, killer, p.getLastDamageCause().getCause());
                        } else {
                            msgFall(PrefixType.INFO, "death." + p.getLastDamageCause().getEntityType(),
                                    "player-" + (BowWarfare.auth.contains(p.getName()) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "")
                                    + p.getName(), "killer-" + p.getLastDamageCause().getEntityType());
                            pk = new PlayerKilledEvent(p, this, null, p.getLastDamageCause().getCause());
                        }
                        break;
                    default:
                        msgFall(PrefixType.INFO, "death." + p.getLastDamageCause().getCause().name(),
                                "player-" + (BowWarfare.auth.contains(p.getName()) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "") + p.getName(),
                                "killer-" + ((killer != null) ? ((BowWarfare.auth.contains(killer.getName()) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "") + killer.getName()) : "Unknown"));
                        if (killer != null && p != null && activePlayers.contains(killer)) {
                            sm.addKill(killer, gameID);
                        }
                        pk = new PlayerKilledEvent(p, this, (killer != null) ? killer : null, p.getLastDamageCause().getCause());
                        break;
                }

                Bukkit.getServer().getPluginManager().callEvent(pk);
                setGameInventory(p);

                availableGameTypes.get(gametype).checkWin(p, killer);
            }

            LobbyManager.getInstance().updateWall(gameID);

        } catch (Exception e) {
            BowWarfare.$("???????????????????????");
            e.printStackTrace();
            BowWarfare.$("ID" + gameID);
            BowWarfare.$(activePlayers.size() + "");
            BowWarfare.$(activePlayers.toString());
            BowWarfare.$(p.getName());
            BowWarfare.$(p.getLastDamageCause().getCause().name());
        }
    }

    /*
     * 
     * ################################################
     * 
     * 				REMOVE PLAYER
     * 
     * ################################################
     * 
     * 
     */
    public void removePlayer(Player p, boolean... leave) {

        availableGameTypes.get(gametype).onPlayerRemove(p, false);

        if (!activePlayers.contains(p)) {
            return;
        }

        if (state == GameState.INGAME) {
            killPlayer(p, null, true);
        }

        clearInv(p);

        if (leave.length < 1) {
            p.teleport(SettingsManager.getInstance().getLobbySpawn());
        }

        BarAPI.removeBar(p);
        p.setScoreboard(sbManager.getNewScoreboard());

        if (state != GameState.INGAME) {
            sm.removePlayer(p, gameID);
        }

        activePlayers.remove(p);
        inactivePlayers.remove(p);

        restoreInv(p);

        if (activePlayers.size() < 1) {
            endGame();
        }

        msgFall(PrefixType.INFO, "game.playerleavegame", "player-" + p.getName());

        PlayerLeaveArenaEvent pl = new PlayerLeaveArenaEvent(p, this, false);

        LobbyManager.getInstance().updateWall(gameID);
    }

    /*
     * 
     * ################################################
     * 
     * 				PLAYER LEAVE
     * 
     * ################################################
     * 
     * 
     */
    public void playerLeave(Player p) {
        availableGameTypes.get(gametype).onPlayerQuit(p);
        removePlayer(p, true);
    }

    /*
     *
     * ################################################
     *
     *                                 PLAYER WIN
     *
     * ################################################
     *
     *
     */
    public void playerWin(Player victim, Player win) {
        if (state == GameState.DISABLED) {
            return;
        }

        msgmgr.broadcastFMessage(PrefixType.INFO, "game.playerwin", "arena-" + gameID, "victim-" + victim.getName(), "player-" + win.getName());

        LobbyManager.getInstance().display(new String[]{
            win.getName(), "", "Won the ", "Bow Warfare!"
        }, gameID);

        sm.playerWin(win, gameID);
        sm.saveGame(gameID, win, new Date().getTime() - startTime);

        state = GameState.FINISHING;

        for (Player acP : activePlayers) {

            acP.teleport(SettingsManager.getInstance().getLobbySpawn());

            sm.removePlayer(acP, gameID);

            clearInv(acP);
            restoreInv(acP);

            acP.setHealth(acP.getMaxHealth());
            acP.setFoodLevel(20);
            acP.setFireTicks(0);
            acP.setFallDistance(0);

            BarAPI.removeBar(acP);
            acP.setScoreboard(sbManager.getNewScoreboard());
        }

        LobbyManager.getInstance().updateWall(gameID);
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gameend", "arena-" + gameID);

        clearSpectators();
        endGame();
    }

    /*
     * 
     * ################################################
     * 
     * 				DISABLE
     * 
     * ################################################
     * 
     * 
     */
    public void disable() {
        disabled = true;

        for (int a = 0; a < activePlayers.size(); a = 0) {
            try {
                Player p = activePlayers.get(a);
                msgmgr.sendFMessage(PrefixType.WARNING, "game.disablegame", p);
                removePlayer(p);
            } catch (Exception e) {
            }

        }

        for (int a = 0; a < inactivePlayers.size(); a = 0) {
            try {

                Player p = inactivePlayers.remove(a);
                msgmgr.sendFMessage(PrefixType.WARNING, "game.disablegame", p);
            } catch (Exception e) {
            }

        }

        clearSpectators();
        queue.clear();

        endGame();
        LobbyManager.getInstance().updateWall(gameID);
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gamedisabled", "arena-" + gameID);

    }

    /*
     * 
     * ################################################
     * 
     * 				RESET
     * 
     * ################################################
     * 
     * 
     */
    public void resetArena() {

        for (Integer i : tasks) {
            Bukkit.getScheduler().cancelTask(i);
        }

        tasks.clear();
        activePlayers.clear();
        inactivePlayers.clear();
        availableGameTypes.clear();

        vote = 0;
        voted.clear();

        loadAvailableGameModes();

        if (settings.get(SettingsManager.OptionFlag.GAMETYPE) != null) {
            setAnGamemode();
        } else {
            gametype = -1;
        }

        state = GameState.RESETING;

        QueueManager.getInstance().rollback(gameID, false);
        LobbyManager.getInstance().updateWall(gameID);

    }

    /*
     * 
     * ################################################
     * 
     * 				SPECTATOR
     * 
     * ################################################
     * 
     * 
     */
    public void addSpectator(Player p) {
        if (state != GameState.INGAME) {
            msgmgr.sendMessage(PrefixType.WARNING, "You can only spectate running games!", p);
            return;
        }

        saveInv(p);
        clearInv(p);

        //TO FIX: teleport over a player not on the same place
        p.teleport(activePlayers.get(0).getLocation());

        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.hidePlayer(p);
        }

        p.setAllowFlight(true);
        p.setFlying(true);

        spectators.add(p);

        msgmgr.sendMessage(PrefixType.INFO, "You are now spectating! Use /bw spectate again to return to the lobby.", p);
        msgmgr.sendMessage(PrefixType.INFO, "Right click while holding shift to teleport to the next ingame player, left click to go back.", p);

        nextSpectator.put(p, 0);
    }

    public void removeSpectator(Player p) {
        if (p.isOnline()) {
            for (Player pl : Bukkit.getOnlinePlayers()) {
                pl.showPlayer(p);
            }
        }

        restoreInv(p);
        p.setAllowFlight(false);
        p.setFlying(false);
        p.setFallDistance(0);
        p.setHealth(p.getMaxHealth());
        p.setFoodLevel(20);
        p.setSaturation(20);
        p.teleport(SettingsManager.getInstance().getLobbySpawn());

        spectators.remove(p);

        nextSpectator.remove(p);
    }

    public void clearSpectators() {
        while (spectators.size() > 0) {
            removeSpectator(spectators.get(0));
        }

        spectators.clear();
        nextSpectator.clear();
    }

    public HashMap< Player, Integer> getNextSpec() {
        return nextSpectator;
    }

    public void removeFromQueue(Player p) {
        queue.remove(p);
    }

    public void endGame() {
        state = GameState.WAITING;
        resetArena();
        LobbyManager.getInstance().clearSigns(gameID);
        LobbyManager.getInstance().updateWall(gameID);

    }

    public void resetCallback() {
        if (!disabled) {
            enable();
        } else {
            state = GameState.DISABLED;
        }
        LobbyManager.getInstance().updateWall(gameID);
    }

    public void saveInv(Player p) {
        ItemStack[][] store = new ItemStack[2][1];

        store[0] = p.getInventory().getContents();
        store[1] = p.getInventory().getArmorContents();

        inventoryStore.put(p, store);
    }

    public void restoreInvOffline(UUID id) {
        restoreInv(Bukkit.getPlayer(id));
    }

    @SuppressWarnings("deprecation")
    public void restoreInv(Player p) {
        try {
            clearInv(p);
            p.getInventory().setContents(inventoryStore.get(p)[0]);
            p.getInventory().setArmorContents(inventoryStore.get(p)[1]);
            inventoryStore.remove(p);
            p.updateInventory();
        } catch (Exception e) {
        }
    }

    @SuppressWarnings("deprecation")
    public void clearInv(Player p) {
        ItemStack[] inv = p.getInventory().getContents();
        for (int i = 0; i < inv.length; i++) {
            inv[i] = null;
        }
        p.getInventory().setContents(inv);
        inv = p.getInventory().getArmorContents();
        for (int i = 0; i < inv.length; i++) {
            inv[i] = null;
        }
        p.getInventory().setArmorContents(inv);
        p.updateInventory();

    }

    @SuppressWarnings("deprecation")
    public void clearOnlyInv(Player p) {
        ItemStack[] inv = p.getInventory().getContents();
        for (int i = 0; i < inv.length; i++) {
            inv[i] = null;
        }
        p.getInventory().setContents(inv);
        p.updateInventory();
    }

    @SuppressWarnings("deprecation")
    public void setGameInventory(Player p) {
        clearOnlyInv(p);

        ItemStack bow = new ItemStack(Material.BOW);
        ItemStack bullets = new ItemStack(Material.ARROW);
        ItemStack plessureArrow = new ItemStack(Material.GOLD_PLATE, 2);
        ItemStack plessureTNT = new ItemStack(Material.IRON_PLATE, 2);
        ItemStack snowBall = new ItemStack(Material.SNOW_BALL, 3);

        bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        bow.addEnchantment(Enchantment.DURABILITY, 3);

        ItemMeta imBow = bow.getItemMeta();
        ItemMeta imBullets = bullets.getItemMeta();
        ItemMeta imPlessureArrow = plessureArrow.getItemMeta();
        ItemMeta imPlessureTNT = plessureTNT.getItemMeta();
        ItemMeta imSnowBall = snowBall.getItemMeta();

        //TODO - Add message.yml
        imBow.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Weapon");
        imBullets.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Bullets");
        imPlessureArrow.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Explosive Mine");
        imPlessureTNT.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Mine");
        imSnowBall.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Granade");

        bow.setItemMeta(imBow);
        bullets.setItemMeta(imBullets);
        plessureArrow.setItemMeta(imPlessureArrow);
        plessureTNT.setItemMeta(imPlessureTNT);
        snowBall.setItemMeta(imSnowBall);

        p.getInventory().addItem(new ItemStack[]{bow});
        p.getInventory().addItem(new ItemStack[]{bullets});
        //p.getInventory().addItem(new ItemStack[]{plessureArrow});
        p.getInventory().addItem(new ItemStack[]{plessureTNT});
        p.getInventory().addItem(new ItemStack[]{snowBall});

        p.updateInventory();
    }

    public void loadAvailableGameModes() {
        if (new FreeForAll(this, true).tryLoadSpawn()) {
            availableGameTypes.add(new FreeForAll(this));
            BowWarfare.$("Loading Gametype: FFA for Arena " + gameID);
        }

        if (new TeamDeathMatch(this, true).tryLoadSpawn()) {
            availableGameTypes.add(new TeamDeathMatch(this));
            BowWarfare.$("Loading Gametype: TDM for Arena " + gameID);
        }

        if (new LastManStanding(this, true).tryLoadSpawn()) {
            availableGameTypes.add(new LastManStanding(this));
            BowWarfare.$("Loading Gametype: LMS for Arena " + gameID);
        }
    }

    public void setAnGamemode() {
        String strGamemode = "";
        if (settings.get(SettingsManager.OptionFlag.GAMETYPE) != null) {
            strGamemode = (String) settings.get(SettingsManager.OptionFlag.GAMETYPE);

            for (int i = 0; i < availableGameTypes.size(); i++) {
                if (availableGameTypes.get(i).getGametypeName().equalsIgnoreCase(strGamemode)) {
                    gametype = i;
                    break;
                }
            }
        } else if (availableGameTypes.size() > 0) {
            Random random = new Random();
            gametype = random.nextInt(availableGameTypes.size());
        }
    }

    public boolean isBlockInArena(Location v) {
        return arena.containsBlock(v);
    }

    public int getID() {
        return gameID;
    }

    public int getActivePlayers() {
        return activePlayers.size();
    }

    public int getInactivePlayers() {
        return inactivePlayers.size();
    }

    public int getMaxPlayer() {
        return availableGameTypes.get(gametype).getMaxPlayer();
    }

    public Player[][] getPlayers() {
        return new Player[][]{
            activePlayers.toArray(new Player[0]), inactivePlayers.toArray(new Player[0])
        };
    }

    public ArrayList< Player> getAllPlayers() {
        ArrayList< Player> all = new ArrayList< Player>();
        all.addAll(activePlayers);
        all.addAll(inactivePlayers);
        return all;
    }

    public boolean isSpectator(Player p) {
        return spectators.contains(p.getName());
    }

    public boolean isInQueue(Player p) {
        return queue.contains(p);
    }

    public boolean isPlayerActive(Player player) {
        return activePlayers.contains(player);
    }

    public boolean isPlayerinactive(Player player) {
        return inactivePlayers.contains(player);
    }

    public boolean isFrozenSpawn() {
        return availableGameTypes.get(gametype).isFrozenSpawn();
    }

    public boolean hasPlayer(Player p) {
        return activePlayers.contains(p) || inactivePlayers.contains(p);
    }

    public GameState getState() {
        return state;
    }

    public IGametype getGameMode() {
        return (gametype < availableGameTypes.size() && gametype > -1) ? availableGameTypes.get(gametype) : null;
    }

    public boolean isAvailableGameMode(String s) {
        for (IGametype g : availableGameTypes) {
            if (g.getGametypeName().equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public IGametype getAvailableGameType(String s) {
        for (IGametype g : availableGameTypes) {
            if (g.getGametypeName().equalsIgnoreCase(s)) {
                return g;
            }
        }
        return null;
    }

    public boolean markAsInactive(Player p) {
        if (activePlayers.contains(p)) {
            activePlayers.remove(p);
            inactivePlayers.add(p);
            return true;
        }
        return false;
    }

    public void addTask(int scheduleTask) {
        tasks.add(scheduleTask);
    }

    public void removeTask(int scheduleTask) {
        tasks.remove((Integer) scheduleTask);
    }

    public boolean containsTask(int scheduleTask) {
        return tasks.contains((Integer) scheduleTask);
    }

    public synchronized void setRBPercent(double d) {
        rbpercent = d;
    }

    public double getRBPercent() {
        return rbpercent;
    }

    public void setRBStatus(String s) {
        rbstatus = s;
    }

    public String getRBStatus() {
        return rbstatus;
    }

    public String getName() {
        return (String) settings.get(SettingsManager.OptionFlag.ARENA_NAME);
    }

    public World getWorld() {
        return arena.getWorld();
    }

    public void msgFall(PrefixType type, String msg, String... vars) {
        for (Player p : getAllPlayers()) {
            msgmgr.sendFMessage(type, msg, p, vars);
        }
    }
}
