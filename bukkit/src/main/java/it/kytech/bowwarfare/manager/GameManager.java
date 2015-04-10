package it.kytech.bowwarfare.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import it.kytech.bowwarfare.Game.GameState;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;
import it.kytech.bowwarfare.api.PlayerLeaveArenaEvent;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import it.kytech.bowwarfare.BowWarfare;
import it.kytech.bowwarfare.Game;
import org.bukkit.entity.Projectile;

public class GameManager {

    static GameManager instance = new GameManager();
    MessageManager msgmgr = MessageManager.getInstance();
    private ArrayList< Game> games = new ArrayList< Game>();
    private BowWarfare p;
    private HashSet<Player> kitsel = new HashSet<Player>();

    private GameManager() {
    }

    public static GameManager getInstance() {
        return instance;
    }

    public void setup(BowWarfare plugin) {
        p = plugin;
        LoadGames();
    }

    public Plugin getPlugin() {
        return p;
    }

    public void reloadGames() {
        LoadGames();
    }

    public void LoadGames() {
        FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
        games.clear();
        int no = c.getInt("bw-system.arenano", 0);
        int loaded = 0;
        int a = 1;
        while (loaded < no) {
            if (c.isSet("bw-system.arenas." + a + ".x1")) {
                if (c.getBoolean("bw-system.arenas." + a + ".enabled")) {
                    BowWarfare.$("Loading Arena: " + a);
                    loaded++;
                    games.add(new Game(a));
                    StatsManager.getInstance().addArena(a);
                }
            }
            a++;

        }
        LobbyManager.getInstance().clearAllSigns();

    }

    public int getBlockGameId(Location v) {
        for (Game g : games) {
            if (g.isBlockInArena(v)) {
                return g.getID();
            }
        }
        return -1;
    }

    public int getPlayerGameId(Player p) {
        for (Game g : games) {
            if (g.isPlayerActive(p)) {
                return g.getID();
            }
        }
        return -1;
    }

    public int getPlayerSpectateId(Player p) {
        for (Game g : games) {
            if (g.isSpectator(p)) {
                return g.getID();
            }
        }
        return -1;
    }

    public boolean isPlayerActive(Player player) {
        for (Game g : games) {
            if (g.isPlayerActive(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPlayerInactive(Player player) {
        for (Game g : games) {
            if (g.isPlayerActive(player)) {
                return true;
            }
        }
        return false;
    }

    public boolean isSpectator(Player player) {
        for (Game g : games) {
            if (g.isSpectator(player)) {
                return true;
            }
        }
        return false;
    }

    public void removeFromOtherQueues(Player p, int id) {
        for (Game g : getGames()) {
            if (g.isInQueue(p) && g.getID() != id) {
                g.removeFromQueue(p);
                msgmgr.sendMessage(PrefixType.INFO, "Removed from the queue in arena " + g.getID(), p);
            }
        }
    }

    public boolean isInKitMenu(Player p) {
        return kitsel.contains(p);
    }

    public void leaveKitMenu(Player p) {
        kitsel.remove(p);
    }

    public void openKitMenu(Player p) {
        kitsel.add(p);
    }

    public int getGameCount() {
        return games.size();
    }

    public Game getGame(int a) {
        //int t = gamemap.get(a);
        for (Game g : games) {
            if (g.getID() == a) {
                return g;
            }
        }
        return null;
    }

    public void removePlayer(Player p) {
        getGame(getPlayerGameId(p)).removePlayer(p);
    }

    public void leftPlayer(Player p) {
        getGame(getPlayerGameId(p)).playerLeave(p);
    }

    public void killPlayer(Player victim, Player killer) {
        getGame(getPlayerGameId(victim)).killPlayer(victim, killer);
    }

    public boolean blockBreak(Block block, Player p) {
        return getGame(getPlayerGameId(p)).blockBreak(block, p);
    }

    public boolean blockPlace(Block block, Player p) {
        return getGame(getPlayerGameId(p)).blockPlace(block, p);
    }

    public boolean blockInteract(Block block, Player p) {
        return getGame(getPlayerGameId(p)).blockInteract(block, p);
    }

    public boolean projectileHit(Player attacker, Projectile pro) {
        return getGame(getPlayerGameId(attacker)).projectileHit(attacker, pro);
    }

    public void removeSpectator(Player p) {
        getGame(getPlayerSpectateId(p)).removeSpectator(p);
    }

    public boolean isFrozenSpawn(int gameID) {
        return getGame(gameID).isFrozenSpawn();
    }

    public void disableGame(int id) {
        getGame(id).disable();
    }

    public void enableGame(int id) {
        getGame(id).enable();
    }

    public ArrayList< Game> getGames() {
        return games;
    }

    public GameState getGameState(int a) {
        for (Game g : games) {
            if (g.getID() == a) {
                return g.getGameState();
            }
        }
        return null;
    }

    public void startGame(int a) {
        getGame(a).startGame();
    }

    public void addPlayer(Player p, int g) {
        Game game = getGame(g);
        if (game == null) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.input", p, "message-No game by this ID exist!");
            return;
        }
        getGame(g).addPlayer(p);
    }

    public void autoAddPlayer(Player pl) {
        ArrayList<Game> qg = new ArrayList<Game>(5);
        for (Game g : games) {
            if (g.getState() == Game.GameState.WAITING) {
                qg.add(g);
            }
        }
        //TODO: fancy auto balance algorithm
        if (qg.size() == 0) {
            pl.sendMessage(ChatColor.RED + "No games to join");
            msgmgr.sendMessage(PrefixType.WARNING, "No games to join!", pl);
            return;
        }
        qg.get(0).addPlayer(pl);
    }

    public WorldEditPlugin getWorldEdit() {
        return p.getWorldEdit();
    }

    public void createArenaFromSelection(Player pl) {

        FileConfiguration c = SettingsManager.getInstance().getSystemConfig();

        WorldEditPlugin we = p.getWorldEdit();
        Selection sel = we.getSelection(pl);

        if (sel == null) {
            msgmgr.sendMessage(PrefixType.WARNING, "You must make a WorldEdit Selection first!", pl);
            return;
        }

        Location max = sel.getMaximumPoint();
        Location min = sel.getMinimumPoint();

        int no = c.getInt("bw-system.arenano") + 1;
        c.set("bw-system.arenano", no);
        if (games.size() == 0) {
            no = 1;
        } else {
            no = games.get(games.size() - 1).getID() + 1;
        }
        SettingsManager.getInstance().getSpawns().set(("spawns." + no), null);
        c.set("bw-system.arenas." + no + ".world", max.getWorld().getName());
        c.set("bw-system.arenas." + no + ".x1", max.getBlockX());
        c.set("bw-system.arenas." + no + ".y1", max.getBlockY());
        c.set("bw-system.arenas." + no + ".z1", max.getBlockZ());
        c.set("bw-system.arenas." + no + ".x2", min.getBlockX());
        c.set("bw-system.arenas." + no + ".y2", min.getBlockY());
        c.set("bw-system.arenas." + no + ".z2", min.getBlockZ());
        c.set("bw-system.arenas." + no + ".enabled", true);

        SettingsManager.getInstance().saveSystemConfig();

        hotAddArena(no);

        pl.sendMessage(ChatColor.GREEN + "Arena ID " + no + " Succesfully added");

    }

    private void hotAddArena(int no) {
        Game game = new Game(no);
        games.add(game);
        StatsManager.getInstance().addArena(no);
    }

    public void hotRemoveArena(int no) {
        for (Game g : games.toArray(new Game[0])) {
            if (g.getID() == no) {
                games.remove(getGame(no));
            }
        }
    }

    public String getStringList(int gid) {
        Game g = getGame(gid);
        StringBuilder sb = new StringBuilder();
        ArrayList<String> updateSignPlayer = g.getGameMode().updateSignPlayer();

        sb.append(ChatColor.GREEN + "<--------------[ " + updateSignPlayer.size() + " Players | " + g.getGameMode().getGametypeName() + " ]-------------->\n" + ChatColor.GREEN + " ");
        for (String p : updateSignPlayer) {
            sb.append(p.replace(ChatColor.BLACK.toString(), ChatColor.WHITE.toString()) + ",");
        }

        sb.append("\n\n");

        return sb.toString();
    }
}
