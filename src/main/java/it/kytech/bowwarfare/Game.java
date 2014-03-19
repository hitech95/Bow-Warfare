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
import it.kytech.bowwarfare.SettingsManager.OptionFlag;
import it.kytech.bowwarfare.api.PlayerJoinArenaEvent;
import it.kytech.bowwarfare.api.PlayerKilledEvent;
import it.kytech.bowwarfare.api.PlayerLeaveArenaEvent;
import it.kytech.bowwarfare.gametype.FreeForAll;
import it.kytech.bowwarfare.gametype.Gametype;
import it.kytech.bowwarfare.hooks.HookManager;
import it.kytech.bowwarfare.logging.QueueManager;
import it.kytech.bowwarfare.stats.StatsManager;
import it.kytech.bowwarfare.util.ItemReader;
import it.kytech.bowwarfare.util.Kit;
import it.kytech.bowwarfare.util.bossbar.StatusBarAPI;
import java.util.Date;
import java.util.Random;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.scoreboard.ScoreboardManager;

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
    private ArrayList<Gametype> availableGameTypes = new ArrayList<Gametype>();
    private int gametype = -1;
    private FileConfiguration config;
    private FileConfiguration system;
    private HashMap< Player, ItemStack[][]> inv_store = new HashMap< Player, ItemStack[][]>();
    private boolean disabled = false;
    private double rbpercent = 0;
    private String rbstatus = "";
    private long startTime = 0;
    private StatsManager statMan = StatsManager.getInstance();
    private MessageManager msgmgr = MessageManager.getInstance();
    private StatsManager sm = StatsManager.getInstance();
    private ScoreboardManager sbManager = Bukkit.getScoreboardManager();

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

    public void addSpawn(String gamemode, Location l) {
        Gametype gameMode = getAvailableGameMode(gamemode);
        if (gameMode != null) {
            gameMode.addSpawn(l);
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
            int b = (SettingsManager.getInstance().getMaxPlayerCount(gameID, availableGameTypes.get(gametype)) > queue.size()) ? queue.size() : SettingsManager.getInstance().getMaxPlayerCount(gameID, availableGameTypes.get(gametype));

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
            return false;
        }

        if (!p.hasPermission("bw.arena.join." + gameID)) {
            debug("permission needed to join arena: " + "bw.arena.join." + gameID);
            msgmgr.sendFMessage(PrefixType.WARNING, "game.nopermission", p, "arena-" + gameID);
            return false;
        }

        if (gametype > -1) {
            OptionFlag value = SettingsManager.OptionFlag.valueOf(availableGameTypes.get(gametype).getGamemodeName() + "MAXP");
            HookManager.getInstance().runHook("GAME_PRE_ADDPLAYER", "arena-" + gameID, "player-" + p.getName(), "maxplayers-" + settings.get(value), "players-" + activePlayers.size());
        } else {
            HookManager.getInstance().runHook("GAME_PRE_ADDPLAYER", "arena-" + gameID, "player-" + p.getName(), "players-" + activePlayers.size());
        }

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

        boolean hasAdded = false;

        if (state != GameState.DISABLED && state != GameState.RESETING && state != GameState.LOADING && state != GameState.INACTIVE && state != GameState.ERROR) {
            if (gametype < 0) {
                setAnGamemode();
            }
            Gametype currentG = availableGameTypes.get(gametype);
            if (currentG.getSpawnCount() == 0) {
                msgmgr.sendMessage(MessageManager.PrefixType.ERROR, "error.nospawns", p);
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
            msgmgr.sendMessage(PrefixType.INFO, "Joining Arena " + gameID, p);
            p.setHealth(p.getMaxHealth());
            p.setFoodLevel(20);
            clearInv(p);
            activePlayers.add(p);
            sm.addPlayer(p, gameID);

            ItemStack Bow = new ItemStack(Material.BOW);
            Bow.addEnchantment(Enchantment.ARROW_INFINITE, 1);
            Bow.addEnchantment(Enchantment.DURABILITY, 3);

            ItemStack plessureArrow = new ItemStack(Material.GOLD_PLATE, 2);
            ItemStack plessureTNT = new ItemStack(Material.IRON_PLATE, 2);
            ItemStack snowBall = new ItemStack(Material.SNOW_BALL, 3);

            /*
            ItemMeta im = i1.getItemMeta();

            debug(k.getName() + " " + i1 + " " + im);

            im.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + k.getName());
            i1.setItemMeta(im);
            
            */

            p.getInventory().addItem(new ItemStack[]{Bow});
            p.getInventory().addItem(new ItemStack[]{new ItemStack(Material.ARROW)});

            LobbyManager.getInstance().updateWall(gameID);
            showMenu(p);
            HookManager.getInstance().runHook("GAME_POST_ADDPLAYER", "activePlayers-" + activePlayers.size());
            return true;
        }

        if (state == GameState.DISABLED) {
            msgmgr.sendFMessage(PrefixType.WARNING, "error.gamedisabled", p, "arena-" + gameID);
        } else if (state == GameState.RESETING) {
            msgmgr.sendFMessage(PrefixType.WARNING, "error.gamereseting", p);
        } else {
            msgmgr.sendMessage(PrefixType.INFO, "Cannot join game!", p);
        }

        LobbyManager.getInstance().updateWall(gameID);
        return false;
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
        startTime = new Date().getTime();

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

            sm.playerDied(p, activePlayers.size(), gameID, new Date().getTime() - startTime);

            PlayerKilledEvent pk = null;

            if (state != GameState.WAITING && p.getLastDamageCause() != null && p.getLastDamageCause().getCause() != null && leave.length < 1) {
                switch (p.getLastDamageCause().getCause()) {
                    case ENTITY_ATTACK:
                        if (p.getLastDamageCause().getEntityType() == EntityType.PLAYER) {
                            msgFall(PrefixType.INFO, "death." + p.getLastDamageCause().getEntityType(),
                                    "player-" + (BowWarfare.auth.contains(p.getName()) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "") + p.getName(),
                                    "killer-" + ((killer != null) ? ((BowWarfare.auth.contains(killer.getName()) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "") + killer.getName()) : "Unknown"),
                                    "item-" + ((killer != null) ? ItemReader.getFriendlyItemName(killer.getItemInHand().getType()) : "Unknown Item"));
                            if (killer != null && p != null && activePlayers.contains(killer)) {
                                sm.addKill(killer, p, gameID);
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
                            sm.addKill(killer, p, gameID);
                        }
                        pk = new PlayerKilledEvent(p, this, (killer != null) ? killer : null, p.getLastDamageCause().getCause());

                        break;
                }
                Bukkit.getServer().getPluginManager().callEvent(pk);

                availableGameTypes.get(gametype).onPlayerKilled(p, killer, leave.length > 0);
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

        StatusBarAPI.removeStatusBar(p);
        p.setScoreboard(sbManager.getNewScoreboard());

        sm.removePlayer(p, gameID);

        activePlayers.remove(p);
        inactivePlayers.remove(p);

        restoreInv(p);

        if (activePlayers.size() < 1 && state != GameState.WAITING) {
            endGame();
        }

        msgFall(PrefixType.INFO, "game.playerleavegame", "player-" + p.getName());

        HookManager.getInstance().runHook("PLAYER_REMOVED", "player-" + p.getName());

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
        if (GameState.DISABLED == state) {
            return;
        }

        msgmgr.broadcastFMessage(PrefixType.INFO, "game.playerwin", "arena-" + gameID, "victim-" + victim.getName(), "player-" + win.getName());

        LobbyManager.getInstance().display(new String[]{
            win.getName(), "", "Won the ", "Bow Warfare!"
        }, gameID);

        sm.playerWin(win, gameID, new Date().getTime() - startTime);
        sm.saveGame(gameID, win, getActivePlayers() + getInactivePlayers(), new Date().getTime() - startTime);

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

            StatusBarAPI.removeStatusBar(acP);
            acP.setScoreboard(sbManager.getNewScoreboard());
        }

        LobbyManager.getInstance().updateWall(gameID);
        MessageManager.getInstance().broadcastFMessage(PrefixType.INFO, "broadcast.gameend", "arena-" + gameID);

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
        activePlayers.clear();
        inactivePlayers.clear();
        availableGameTypes.clear();

        loadAvailableGameModes();

        if (settings.get(SettingsManager.OptionFlag.GAMETYPE) != null) {
            setAnGamemode();
        } else {
            gametype = -1;
        }

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

    public void loadAvailableGameModes() {
        if (new FreeForAll(gameID, true).tryLoadSpawn()) {
            availableGameTypes.add(new FreeForAll(gameID));
        }
    }

    public void setAnGamemode() {
        String strGamemode = "";
        if (settings.get(SettingsManager.OptionFlag.GAMETYPE) != null) {
            strGamemode = (String) settings.get(SettingsManager.OptionFlag.GAMETYPE);

            for (int i = 0; i < availableGameTypes.size(); i++) {
                if (availableGameTypes.get(i).getGamemodeName().equalsIgnoreCase(strGamemode)) {
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

    public Gametype getGameMode() {
        return (gametype < availableGameTypes.size() && gametype > -1) ? availableGameTypes.get(gametype) : null;
    }

    public boolean isAvailableGameMode(String s) {
        for (Gametype g : availableGameTypes) {
            if (g.getGamemodeName().equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    public Gametype getAvailableGameMode(String s) {
        for (Gametype g : availableGameTypes) {
            if (g.getGamemodeName().equalsIgnoreCase(s)) {
                return g;
            }
        }
        return null;
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
