package it.kytech.bowwarfare;

import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import it.kytech.bowwarfare.MessageManager.PrefixType;
import it.kytech.bowwarfare.api.PlayerLeaveArenaEvent;
import it.kytech.bowwarfare.gamemods.FreeForAll;
import it.kytech.bowwarfare.gamemods.Gamemode;
import it.kytech.bowwarfare.hooks.HookManager;
import it.kytech.bowwarfare.logging.QueueManager;
import it.kytech.bowwarfare.stats.StatsManager;
import it.kytech.bowwarfare.util.Kit;
import java.util.Date;
import java.util.Random;
import org.bukkit.block.Block;

//Data container for a game
public class Game {

    public static enum GameState {

        DISABLED, LOADING, INACTIVE, WAITING,
        STARTING, INGAME, FINISHING, RESETING, ERROR
    }
    private GameState state = GameState.DISABLED;
    private ArrayList< Player> activePlayers = new ArrayList< Player>();
    private ArrayList< Player> inactivePlayers = new ArrayList< Player>();
    private ArrayList< String> spectators = new ArrayList< String>();
    HashMap< Player, Integer> nextspec = new HashMap< Player, Integer>();
    private ArrayList< Player> queue = new ArrayList< Player>();
    private HashMap<SettingsManager.OptionFlag, Object> settings = new HashMap<SettingsManager.OptionFlag, Object>();
    private ArrayList<Integer> tasks = new ArrayList<Integer>();
    private Arena arena;
    private int gameID;
    private int gcount = 0;
    private ArrayList<Gamemode> availableGameModes = new ArrayList<Gamemode>();
    private int gamemode = -1;
    private FileConfiguration config;
    private FileConfiguration system;
    private HashMap< Player, ItemStack[][]> inv_store = new HashMap< Player, ItemStack[][]>();
    private boolean disabled = false;
    private double rbpercent = 0;
    private String rbstatus = "";
    private long startTime = 0;
    private StatsManager statMan = StatsManager.getInstance();
    private HashMap< String, String> hookvars = new HashMap< String, String>();
    private MessageManager msgmgr = MessageManager.getInstance();
    private StatsManager sm = StatsManager.getInstance();

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

        hookvars.put("arena", gameID + "");
        hookvars.put("maxplayers", settings.get(SettingsManager.OptionFlag.MAX_PLAYERS) + "");
        hookvars.put("activeplayers", "0");

        availableGameModes();

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

        int b = (SettingsManager.getInstance().getMaxPlayerCount(gameID) > queue.size()) ? queue.size() : SettingsManager.getInstance().getMaxPlayerCount(gameID);

        for (int a = 0; a < b; a++) {
            addPlayer(queue.remove(0));
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
            return false;
        }

        if (!p.hasPermission("bw.arena.join." + gameID)) {
            debug("permission needed to join arena: " + "bw.arena.join." + gameID);
            msgmgr.sendFMessage(PrefixType.WARNING, "game.nopermission", p, "arena-" + gameID);
            return false;
        }

        HookManager.getInstance().runHook("GAME_PRE_ADDPLAYER", "arena-" + gameID, "player-" + p.getName(), "maxplayers-" + settings.get(SettingsManager.OptionFlag.MAX_PLAYERS), "players-" + activePlayers.size());

        GameManager.getInstance().removeFromOtherQueues(p, gameID);

        if (GameManager.getInstance().getPlayerGameId(p) != -1) {
            if (GameManager.getInstance().isPlayerActive(p)) {
                msgmgr.sendMessage(PrefixType.ERROR, "Cannot join multiple games!", p);
                return false;
            }
        }

        if (p.isInsideVehicle()) {
            p.leaveVehicle();
        }

        if (spectators.contains(p)) {
            removeSpectator(p);
        }

        if (state == GameState.DISABLED) {
            msgmgr.sendFMessage(PrefixType.WARNING, "error.gamedisabled", p, "arena-" + gameID);
        } else if (state == GameState.RESETING) {
            msgmgr.sendFMessage(PrefixType.WARNING, "error.gamereseting", p);
        } else {
            msgmgr.sendMessage(PrefixType.INFO, "Cannot join game!", p);
        }

        //Se non è possibile aggiungere il player lo aggiungo in coda chiamata a funzione
        if (activePlayers.size() < 1) {
            setAnGamemode();
        }

        Gamemode currentG = availableGameModes.get(gamemode);

        boolean hasAdded = false;

        if (activePlayers.size() < (Integer) settings.get(SettingsManager.OptionFlag.MAX_PLAYERS)) {
            hasAdded = currentG.onJoin(p);
        } else if (currentG.getSpawnCount() == 0) {
            msgmgr.sendMessage(MessageManager.PrefixType.ERROR, "error.nospawns", p);
        } else if (activePlayers.size() == (Integer) settings.get(SettingsManager.OptionFlag.MAX_PLAYERS)) {
            msgmgr.sendFMessage(PrefixType.WARNING, "error.gamefull", p, "arena-" + gameID);
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
        }

        LobbyManager.getInstance().updateWall(gameID);
        return hasAdded;
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

        if (activePlayers.size() <= 0) {
            for (Player pl : activePlayers) {
                msgmgr.sendMessage(PrefixType.WARNING, "Not enough players!", pl);
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
        return availableGameModes.get(gamemode).onBlockBreaked(block, p);
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
        return availableGameModes.get(gamemode).onBlockPlaced(block, p);
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
    public void killPlayer(Player p) {
        availableGameModes.get(gamemode).onPlayerKilled(p, false);
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
    public void removePlayer(Player p) {
        availableGameModes.get(gamemode).onPlayerRemove(p, false);
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
        if (state == GameState.INGAME) {
            availableGameModes.get(gamemode).onPlayerKilled(p, true);
        } else {
            availableGameModes.get(gamemode).onPlayerRemove(p, true);
        }
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
                msgmgr.sendMessage(PrefixType.WARNING, "Game disabled!", p);
                removePlayer(p);
            } catch (Exception e) {
            }

        }

        for (int a = 0; a < inactivePlayers.size(); a = 0) {
            try {

                Player p = inactivePlayers.remove(a);
                msgmgr.sendMessage(PrefixType.WARNING, "Game disabled!", p);
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

        state = GameState.RESETING;

        GameManager.getInstance().gameEndCallBack(gameID);
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

        HookManager.getInstance().runHook("PLAYER_SPECTATE", "player-" + p.getName());

        for (Player pl : Bukkit.getOnlinePlayers()) {
            pl.hidePlayer(p);
        }

        p.setAllowFlight(true);
        p.setFlying(true);

        spectators.add(p.getName());

        msgmgr.sendMessage(PrefixType.INFO, "You are now spectating! Use /bw spectate again to return to the lobby.", p);
        msgmgr.sendMessage(PrefixType.INFO, "Right click while holding shift to teleport to the next ingame player, left click to go back.", p);

        nextspec.put(p, 0);
    }

    public void removeSpectator(Player p) {
        ArrayList< Player> players = new ArrayList< Player>();
        players.addAll(activePlayers);
        players.addAll(inactivePlayers);

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

        spectators.remove(p.getName());

        nextspec.remove(p);
    }

    public void clearSpectators() {
        for (int a = 0; a < spectators.size(); a = 0) {
            removeSpectator(Bukkit.getPlayerExact(spectators.get(0)));
        }

        spectators.clear();
        nextspec.clear();
    }

    public HashMap< Player, Integer> getNextSpec() {
        return nextspec;
    }

    public void showMenu(Player p) {
        GameManager.getInstance().openKitMenu(p);
        Inventory i = Bukkit.getServer().createInventory(p, 90, ChatColor.RED + "" + ChatColor.BOLD + "Kit Selection");

        int a = 0;
        int b = 0;


        ArrayList<Kit> kits = GameManager.getInstance().getKits(p);
        BowWarfare.debug(kits + "");
        if (kits == null || kits.size() == 0 || !SettingsManager.getInstance().getKits().getBoolean("enabled")) {
            GameManager.getInstance().leaveKitMenu(p);
            return;
        }

        for (Kit k : kits) {
            ItemStack i1 = k.getIcon();
            ItemMeta im = i1.getItemMeta();

            debug(k.getName() + " " + i1 + " " + im);

            im.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + k.getName());
            i1.setItemMeta(im);
            i.setItem((9 * a) + b, i1);
            a = 2;

            for (ItemStack s2 : k.getContents()) {
                if (s2 != null) {
                    i.setItem((9 * a) + b, s2);
                    a++;
                }
            }

            a = 0;
            b++;
        }
        p.openInventory(i);
        debug("Showing menu");
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

        inv_store.put(p, store);
    }

    public void restoreInvOffline(String p) {
        restoreInv(Bukkit.getPlayer(p));
    }

    @SuppressWarnings("deprecation")
    public void restoreInv(Player p) {
        try {
            clearInv(p);
            p.getInventory().setContents(inv_store.get(p)[0]);
            p.getInventory().setArmorContents(inv_store.get(p)[1]);
            inv_store.remove(p);
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

    public void availableGameModes() {
        ArrayList Test = new ArrayList();
        ArrayList Test2 = new ArrayList();

        Test = SpawnManager.getInstance().loadSpawns(gameID, "FFA", "");
        if (Test != null) {
            availableGameModes.add(new FreeForAll());
        }

        Test = SpawnManager.getInstance().loadSpawns(gameID, "TDM", "red");
        Test2 = SpawnManager.getInstance().loadSpawns(gameID, "TDM", "blue");
        if ((Test != null) && (Test2 != null)) {
            //availableGameModes.add(new TeamDeathMatch());
        }

        Test = SpawnManager.getInstance().loadSpawns(gameID, "CTF", "red");
        Test2 = SpawnManager.getInstance().loadSpawns(gameID, "CTF", "blue");
        if ((Test != null) && (Test2 != null)) {
            //availableGameModes.add(new CaptureTheFlag());
        }

        Test = SpawnManager.getInstance().loadSpawns(gameID, "INF", "red");
        Test2 = SpawnManager.getInstance().loadSpawns(gameID, "INF", "blue");
        if ((Test != null) && (Test2 != null)) {
            //availableGameModes.add(new Infection());
        }
    }

    public void setAnGamemode() {
        String strGamemode = "";

        if (settings.get(SettingsManager.OptionFlag.GAMEMODE) != null) {
            strGamemode = (String) settings.get(SettingsManager.OptionFlag.GAMEMODE);

            for (int i = 0; i < availableGameModes.size(); i++) {
                if (availableGameModes.get(i).getGamemodeName().equals(strGamemode)) {
                    gamemode = i;
                    break;
                }
            }
        } else if(availableGameModes.size() > 0){
            Random random = new Random();
            gamemode = random.nextInt(availableGameModes.size());
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
        return Integer.parseInt((String) settings.get(SettingsManager.OptionFlag.MAX_PLAYERS));
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
        return availableGameModes.get(gamemode).isFrozenSpawn();
    }

    public boolean hasPlayer(Player p) {
        return activePlayers.contains(p) || inactivePlayers.contains(p);
    }

    public GameState getState() {
        return state;
    }

    public Gamemode getGameMode() {
        return (gamemode < availableGameModes.size() && gamemode > -1)?availableGameModes.get(gamemode):null;
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

    public void msgFall(PrefixType type, String msg, String... vars) {
        for (Player p : getAllPlayers()) {
            msgmgr.sendFMessage(type, msg, p, vars);
        }
    }
}