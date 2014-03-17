/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 *
 * @author M2K
 */
public class SpawnManager {

    private static SpawnManager instance = new SpawnManager();

    public SpawnManager() {
    }

    public static SpawnManager getInstance() {
        return instance;
    }

    public void setSpawn(Location l, int sID, int gameID, String gameType, String... args) {
        gameType = gameType.toUpperCase();
        SettingsManager s = SettingsManager.getInstance();
        FileConfiguration spawns = s.getSpawns();
        StringBuilder str = new StringBuilder("spawns." + gameID + "." + gameType);

        for (int i = 0; i < args.length; i++) {
            str.append("." + args[i].toLowerCase());
        }

        spawns.set(str.toString() + "." + sID + ".x", l.getBlockX());
        spawns.set(str.toString() + "." + sID + ".y", l.getBlockY());
        spawns.set(str.toString() + "." + sID + ".z", l.getBlockZ());
        spawns.set(str.toString() + "." + sID + ".yaw", l.getYaw());
        spawns.set(str.toString() + "." + sID + ".pitch", l.getPitch());

        if (sID > getNumberOf(gameID, gameType, args)) {
            spawns.set(str.toString() + ".count", sID);
        }

        s.saveSpawns();

        GameManager.getInstance().getGame(gameID).addSpawn(gameType, l);
    }

    public int getNumberOf(int gameID, String gameType, String... args) {
        SettingsManager s = SettingsManager.getInstance();
        FileConfiguration spawns = s.getSpawns();
        StringBuilder str = new StringBuilder("spawns." + gameID + "." + gameType.toUpperCase());
        for (int i = 0; i < args.length; i++) {
            str.append("." + args[i].toLowerCase());
        }
        str.append(".count");
        return spawns.getInt(str.toString());
    }

    public ArrayList<Location> loadSpawns(int gameID, String gameType, String... args) {
        gameType = gameType.toUpperCase();
        ArrayList list = new ArrayList();
        SettingsManager s = SettingsManager.getInstance();
        FileConfiguration spawns = s.getSpawns();
        StringBuilder str = new StringBuilder("spawns." + gameID + "." + gameType);
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
            str.append("." + args[i]);
        }
        int count = getNumberOf(gameID, gameType);
        for (int i = 1; i <= count; i++) {
            list.add(new Location(SettingsManager.getGameWorld(gameID),
                    spawns.getInt(str.toString() + "." + i + ".x"),
                    spawns.getInt(str.toString() + "." + i + ".y"),
                    spawns.getInt(str.toString() + "." + i + ".z"),
                    (float) spawns.getDouble(str.toString() + "." + i + ".yaw"),
                    (float) spawns.getDouble(str.toString() + "." + i + ".pitch")));
        }
        return list;
    }
}
