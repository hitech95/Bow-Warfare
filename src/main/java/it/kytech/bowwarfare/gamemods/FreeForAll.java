/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.gamemods;

import it.kytech.bowwarfare.SpawnManager;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

/**
 *
 * @author M2K
 */
public class FreeForAll implements Gamemode {

    public static final String name = "FFA";
    private int gameID;
    public ArrayList<Location> FFASpawns;

    public FreeForAll(int gameID) {
        this.gameID = gameID;
        
        FFASpawns = SpawnManager.getInstance().loadSpawns(gameID, "FFA", "");
    }

    public FreeForAll() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onJoin(Player player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onLeave(Player player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onKill(Player killer, Player victim) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onArrowHit(Player attacker, Arrow arrow) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getGamemodeName() {
        return name;
    }

    @Override
    public int getSpawnCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Location getRandomSpawnPoint() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
