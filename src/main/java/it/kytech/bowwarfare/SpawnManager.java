/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author M2K
 */
public class SpawnManager {

    private static SpawnManager instance = new SpawnManager();

    private SpawnManager() {
    }

    public static SpawnManager getInstance() {
        return instance;
    }

    public ArrayList<Location> loadSpawns(int gameID, String gameType, String team) {
        ArrayList list = new ArrayList();
		//TODO
        return list;
    }
}
