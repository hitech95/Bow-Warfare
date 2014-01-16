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

    public void setNext(Player p, int arena, String gameType) {
        setNext(p, arena, gameType, "");
    }

    public void setNext(Player p, int arena, String gameType, String ... args) {
    }

    public void deleteLast(CommandSender sender, int arena, String gameType) {
        deleteLast(sender, arena, gameType, "");
    }

    public void deleteLast(CommandSender sender, int arena, String gameType, String ... args) {
    }

    public void deleteAll(CommandSender sender, int arena, String gameType) {
        deleteAll(sender, arena, gameType, "");
    }

    public void deleteAll(CommandSender sender, int arena, String gameType, String ... args) {
    }

    public boolean spawn(Player p, int Arena) {
        return spawn(p, Arena, "");
    }

    public boolean spawn(Player p, int Arena, String ... args) {
        return false;
    }

    public int getNumberOf(int Arena, String GameType) {
        return getNumberOf(Arena, GameType, "");
    }

    public int getNumberOf(int Arena, String GameType, String ... args) {
        return 0;
    }

    public ArrayList<Location> loadSpawns(int gameID, String gameType, String ... args) {
        ArrayList list = new ArrayList();
        //TODO
        return null;
    }
}
