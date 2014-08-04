package it.kytech.bowwarfare.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.bukkit.selections.Selection;
import it.kytech.bowwarfare.BowWarfare;
import it.kytech.bowwarfare.LobbyWall;

public class LobbyManager {

    private static LobbyManager instance = new LobbyManager();
    private HashMap< Integer, ArrayList< LobbyWall>> signs = new HashMap< Integer, ArrayList< LobbyWall>>();
    public static HashSet< Chunk> lobbychunks = new HashSet< Chunk>();
    FileConfiguration s = SettingsManager.getInstance().getSystemConfig();

    private LobbyManager() {

    }

    public static LobbyManager getInstance() {
        return instance;
    }

    public void setup(BowWarfare p) {
        loadSigns();
    }

    public void loadSigns() {

        for (int a = 1; a <= s.getInt("bw-system.lobby.signno"); a++) {
            loadSign(a);
        }
    }

    public void loadSign(int a) {
        try {
            BowWarfare.debug("bw-system.lobby.signs." + a + ".world");
            World w = Bukkit.getWorld(s.getString("bw-system.lobby.signs." + a + ".world"));
            int x1 = s.getInt("bw-system.lobby.signs." + a + ".x1");
            int y1 = s.getInt("bw-system.lobby.signs." + a + ".y1");
            int z1 = s.getInt("bw-system.lobby.signs." + a + ".z1");
            int x2 = s.getInt("bw-system.lobby.signs." + a + ".x2");
            //int y2 = s.getInt("bw-system.lobby.signs." + a + ".y2");
            int z2 = s.getInt("bw-system.lobby.signs." + a + ".z2");
            int gameid = s.getInt("bw-system.lobby.signs." + a + ".id");

            LobbyWall ls = new LobbyWall(gameid);
            if (ls.loadSign(w, x1, x2, z1, z2, y1)) {
                ArrayList< LobbyWall> t = signs.get(gameid);
                if (t == null) {
                    t = new ArrayList< LobbyWall>();
                    signs.put(gameid, t);
                }
                t.add(ls);
                ls.update(); //TODO
            } else {
                /*s.set("bw-system.lobby.signs." + a, null); 
                 SettingsManager.getInstance().saveSystemConfig();*/
            }
        } catch (Exception e) {
            s.set("bw-system.lobby.signs." + a, null);
            s.set("bw-system.lobby.signno", s.getInt("bw-system.lobby.signno") - 1);
        }
    }

    /*	class updater implements Runnable{
     public void run(){
     updateall();
     }
     }*/
    public void updateAll() {
        for (ArrayList< LobbyWall> lws : signs.values()) {
            for (LobbyWall lw : lws) {
                lw.update();
            }
        }
    }

    public void updateWall(int a) {
        if (signs.get(a) != null) {
            for (LobbyWall lw : signs.get(a)) {
                lw.update();
            }
        }
    }

    public void removeSignsForArena(int a) {
        clearAllSigns();
        signs.remove(a);
        updateAll();
    }

    public void clearSigns(int a) {
        if (signs.get(a) != null) {
            for (LobbyWall ls : signs.get(a)) {
                ls.clear();
            }
        }
    }

    public void clearAllSigns() {
        for (ArrayList< LobbyWall> lws : signs.values()) {
            for (LobbyWall lw : lws) {
                lw.clear();
            }
        }
    }

    public void display(String s, int a) {
        if (signs.get(a) != null) {

            for (LobbyWall ls : signs.get(a)) {
                ls.addMsg(s);
            }
        }
    }

    public void display(String[] s, int a) {
        for (String s1 : s) {
            display(s1, a);
        }
    }

    public void setLobbySignsFromSelection(Player pl, int a) {
        FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
        SettingsManager s = SettingsManager.getInstance();
        if (!c.getBoolean("bw-system.lobby.sign.set", false)) {
            c.set("bw-system.lobby.sign.set", true);
            s.saveSystemConfig();
        }
        WorldEditPlugin we = GameManager.getInstance().getWorldEdit();
        Selection sel = we.getSelection(pl);
        if (sel == null) {
            pl.sendMessage(ChatColor.RED + "You must make a WorldEdit Selection first");
            return;
        }
        if ((sel.getNativeMaximumPoint().getBlockX() - sel.getNativeMinimumPoint().getBlockX()) != 0 && (sel.getNativeMinimumPoint().getBlockZ() - sel.getNativeMaximumPoint().getBlockZ() != 0)) {
            pl.sendMessage(ChatColor.RED + " Must be in a straight line!");
            return;
        }
        Vector max = sel.getNativeMaximumPoint();
        Vector min = sel.getNativeMinimumPoint();
        int i = c.getInt("bw-system.lobby.signno", 0) + 1;
        c.set("bw-system.lobby.signno", i);
        c.set("bw-system.lobby.signs." + i + ".id", a);
        c.set("bw-system.lobby.signs." + i + ".world", pl.getWorld().getName());
        c.set("bw-system.lobby.signs." + i + ".x1", max.getBlockX());
        c.set("bw-system.lobby.signs." + i + ".y1", max.getBlockY());
        c.set("bw-system.lobby.signs." + i + ".z1", max.getBlockZ());
        c.set("bw-system.lobby.signs." + i + ".x2", min.getBlockX());
        c.set("bw-system.lobby.signs." + i + ".y2", min.getBlockY());
        c.set("bw-system.lobby.signs." + i + ".z2", min.getBlockZ());
        pl.sendMessage(ChatColor.GREEN + "Added Lobby Wall"); //TODO
        s.saveSystemConfig();
        loadSign(i);
    }

    public void deleteLobbySignsFromSelection(Player pl) {
        FileConfiguration c = SettingsManager.getInstance().getSystemConfig();
        SettingsManager s = SettingsManager.getInstance();

        if (!c.getBoolean("bw-system.lobby.sign.set", false)) {
            return;
        }

        WorldEditPlugin we = GameManager.getInstance().getWorldEdit();
        Selection sel = we.getSelection(pl);

        if (sel == null) {
            pl.sendMessage(ChatColor.RED + "You must make a WorldEdit Selection first");
            return;
        }

        if ((sel.getNativeMaximumPoint().getBlockX() - sel.getNativeMinimumPoint().getBlockX()) != 0 && (sel.getNativeMinimumPoint().getBlockZ() - sel.getNativeMaximumPoint().getBlockZ() != 0)) {
            pl.sendMessage(ChatColor.RED + " Must be in a straight line!");
            return;
        }

        Vector max = sel.getNativeMaximumPoint();
        Vector min = sel.getNativeMinimumPoint();

        for (int i = 0; i < c.getInt("bw-system.lobby.signno", 0) + 1; i++) {
            String path = "bw-system.lobby.signs." + i + ".";
            if (c.getInt(path + "x1", 0) == max.getBlockX()
                    && c.getInt(path + "y1", 0) == max.getBlockY()
                    && c.getInt(path + "z1", 0) == max.getBlockZ()
                    && c.getInt(path + "x2", 0) == min.getBlockX()
                    && c.getInt(path + "y2", 0) == min.getBlockY()
                    && c.getInt(path + "z2", 0) == min.getBlockZ()) {
                c.set(path + "enabled", false);
                return;
            }
        }
        s.saveSystemConfig();
    }
}