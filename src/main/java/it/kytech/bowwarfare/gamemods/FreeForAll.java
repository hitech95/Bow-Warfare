/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.gamemods;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.MessageManager.PrefixType;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.SpawnManager;
import it.kytech.bowwarfare.api.PlayerJoinArenaEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Bukkit;
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
    private MessageManager msgmgr = MessageManager.getInstance();

    public FreeForAll(int gameID) {
        this.gameID = gameID;

        FFASpawns = SpawnManager.getInstance().loadSpawns(gameID, "FFA", "");
    }

    public FreeForAll(int gameID, boolean isTest) {
        this.gameID = gameID;

        if (isTest) {
            FFASpawns = null;
            killStreak = null;
            kills = null;
            deaths = null;
            allowedPlace = null;
            allowedBreak = null;
        }
    }

    @Override
    public boolean onJoin(Player player) {
        System.out.println("-----------------Qui Arrivo");
        msgmgr.sendMessage(PrefixType.INFO, "Joining Arena " + gameID, player);        

        player.teleport(getRandomSpawnPoint());
        GameManager.getInstance().getGame(gameID).setState(Game.GameState.INGAME);
        return true;
    }

    @Override
    public boolean onPlayerKilled(Player player, boolean hasLeft) {
        if(!hasLeft){
            player.teleport(getRandomSpawnPoint());
        }        
        return true;
    }

    @Override
    public boolean onPlayerRemove(Player player, boolean hasLeft) {
        return true;
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
        return FFASpawns.size();
    }

    @Override
    public Location getRandomSpawnPoint() {
        System.out.println("--------" + FFASpawns.size());
        Random r = new Random();
        return FFASpawns.get(r.nextInt(FFASpawns.size()));
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
        }
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
        return (SpawnManager.getInstance().getNumberOf(gameID, NAME) > 0) ? true : false;
    }

    @Override
    public void addSpawn(Location l) {
        FFASpawns.add(l);
    }
}
