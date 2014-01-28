/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.gametype;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.MessageManager.PrefixType;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.SpawnManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

/**
 *
 * @author M2K
 */
public class FreeForAll implements Gametype {

    public static final String NAME = "FFA";
    private int gameID;
    private boolean isTest = false;
    private ArrayList<Location> FFASpawns;
    private ArrayList<Integer> allowedPlace = new ArrayList<Integer>();
    private ArrayList<Integer> allowedBreak = new ArrayList<Integer>();
    private HashMap<Player, Integer> kills = new HashMap<Player, Integer>();
    private MessageManager msgmgr = MessageManager.getInstance();

    public FreeForAll(int gameID) {
        isTest = false;
        this.gameID = gameID;

        FFASpawns = SpawnManager.getInstance().loadSpawns(gameID, "FFA", "");
        loadConfig();
    }

    public FreeForAll(int gameID, boolean isTest) {
        this.gameID = gameID;
        this.isTest = isTest;

        if (isTest) {
            FFASpawns = null;
            allowedPlace = null;
            allowedBreak = null;
        }
    }

    private void loadConfig() {

    }

    private void loadDefaultConfig() {

    }

    @Override
    public boolean onJoin(Player player) {
        player.teleport(getRandomSpawnPoint());
        GameManager.getInstance().getGame(gameID).setState(Game.GameState.INGAME);
        return true;
    }

    @Override
    public boolean onPlayerKilled(Player player, boolean hasLeft) {
        Game game = GameManager.getInstance().getGame(gameID);
        if (!hasLeft) {
            int kill = kills.get(player.getKiller()) + 1;
            if (kill >= 5) {
                game.playerWin(player);
                return true;
            } else {
                kills.put(player.getKiller(), kills.get(player.getKiller()) + 1);
            }

            player.teleport(getRandomSpawnPoint());
        }
        return true;
    }

    @Override
    public boolean onPlayerRemove(Player player, boolean hasLeft) {
        return true;
    }

    @Override
    public boolean onPlayerQuit(Player p) {
        return false;
    }

    @Override
    public boolean onArrowHit(Player attacker, Arrow arrow) {
        return false;
    }    

    @Override
    public String getGamemodeName() {
        return NAME;
    }

    @Override
    public int getSpawnCount() {
        return FFASpawns.size();
    }

    @Override
    public Location getRandomSpawnPoint() {
        Random r = new Random();
        return FFASpawns.get(r.nextInt(FFASpawns.size()));
    }

    @Override
    public void updateSingInfo(Sign s) {
        Game game = GameManager.getInstance().getGame(gameID);

        s.setLine(0, NAME);
        s.setLine(1, game.getState() + "");
        s.setLine(2, game.getActivePlayers() + "/" + game.getMaxPlayer());
        s.setLine(3, "");

        if (game.getState() == Game.GameState.RESETING || game.getState() == Game.GameState.FINISHING) {
            s.setLine(3, game.getRBStatus());
            if (game.getRBPercent() > 100) {
                s.setLine(1, "Saving Queue");
                s.setLine(3, (int) game.getRBPercent() + " left");
            } else {
                s.setLine(3, (int) game.getRBPercent() + "%");
            }
        }
    }

    @Override
    public void updateSignPlayer(Sign s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onBlockBreaked(Block block, Player p) {
        return allowedBreak.contains(block.getTypeId());
    }

    @Override
    public boolean onBlockPlaced(Block block, Player p) {
        return allowedPlace.contains(block.getTypeId());
    }

    @Override
    public boolean isFrozenSpawn() {
        return false;
    }

    @Override
    public boolean tryLoadSpawn() {
        return (SpawnManager.getInstance().getNumberOf(gameID, NAME) > 0);
    }

    @Override
    public void addSpawn(Location l) {
        FFASpawns.add(l);
    }
}
