/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.gamemods;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.SpawnManager;
import java.util.ArrayList;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

/**
 *
 * @author M2K
 */
public class FreeForAll implements Gamemode {

    public static final String NAME = "FFA";
    private int gameID;
    public ArrayList<Location> FFASpawns;
    public HashMap<Player, Integer> killStreak = new HashMap();
    public HashMap<Player, Integer> kills = new HashMap();
    public HashMap<Player, Integer> deaths = new HashMap();
    public ArrayList<Integer> allowedPlace = new ArrayList<Integer>();
    public ArrayList<Integer> allowedBreak = new ArrayList<Integer>();

    public FreeForAll(int gameID) {
        this.gameID = gameID;

        FFASpawns = SpawnManager.getInstance().loadSpawns(gameID, "FFA", "");
    }

    public FreeForAll() {
        allowedPlace.add(gameID);
        allowedBreak.add(gameID);

    }

    @Override
    public boolean onJoin(Player player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onPlayerKilled(Player player, boolean hasLeft) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onPlayerRemove(Player player, boolean hasLeft) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onArrowHit(Player attacker, Arrow arrow) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getGamemodeName() {
        return NAME;
    }

    @Override
    public int getSpawnCount() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Location getRandomSpawnPoint() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void updateSingInfo(Sign s) {
        Game game = GameManager.getInstance().getGame(gameID);

        s.setLine(0, game.getName());
        s.setLine(1, NAME);
        s.setLine(2, game.getState() + "");
        s.setLine(3, game.getActivePlayers() + "/" + game.getMaxPlayer());

        //live update
        if (game.getState() == Game.GameState.RESETING || game.getState() == Game.GameState.FINISHING) {
            s.setLine(3, game.getRBStatus());
            if (game.getRBPercent() > 100) {
                s.setLine(1, "Saving Queue");
                s.setLine(3, (int) game.getRBPercent() + " left");
            } else {
                s.setLine(3, (int) game.getRBPercent() + "%");
            }
        } else {
            s.setLine(3, "");
        }
    }

    @Override
    public boolean onBlockBreaked(Block block, Player p) {
        if (!allowedBreak.contains(block.getTypeId())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onBlockPlaced(Block block, Player p) {
        if (!allowedPlace.contains(block.getTypeId())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isFrozenSpawn() {
        return false;
    }
}
