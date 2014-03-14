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
import it.kytech.bowwarfare.util.NameUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK;

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
    private HashMap<SettingsManager.OptionFlag, Object> settings = new HashMap<SettingsManager.OptionFlag, Object>();
    private Random r = new Random();

    private final int DEFAULT_MAXP = 16;
    private final int DEFAULT_KILL = 25;

    public FreeForAll(int gameID) {
        isTest = false;
        this.gameID = gameID;

        FFASpawns = SpawnManager.getInstance().loadSpawns(gameID, NAME, "");
        loadSettings();
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

    private void loadSettings() {
        if (!SettingsManager.getInstance().isSetGameSettings(gameID, this)) {
            loadDefaultSettings();
        } else {
            settings = SettingsManager.getInstance().getGameSettings(gameID);
        }
    }

    private void loadDefaultSettings() {
        settings.put(SettingsManager.OptionFlag.FFAMAXP, DEFAULT_MAXP);
        settings.put(SettingsManager.OptionFlag.FFAKILL, DEFAULT_KILL);

        saveConfig();
    }

    private void saveConfig() {
        SettingsManager.getInstance().saveGameSettings(settings, gameID);
    }

    @Override
    public boolean onJoin(Player player) {
        player.teleport(getRandomSpawnPoint());

        if (GameManager.getInstance().getGame(gameID).getState() != Game.GameState.INGAME) {
            GameManager.getInstance().getGame(gameID).startGame();
        }

        return true;
    }

    @Override
    public boolean onPlayerKilled(Player victim, Player killer, boolean hasLeft) {
        Game game = GameManager.getInstance().getGame(gameID);
        if (!hasLeft) {

            if (kills.get(killer) == null) {
                kills.put(killer, 0);
            }
            int kill = kills.get(killer) + 1;

            if (kill >= (Integer) settings.get(SettingsManager.OptionFlag.FFAKILL)) {
                game.playerWin(victim, killer);
                return true;
            } else {
                kills.put(killer, kills.get(killer) + 1);
            }

            victim.teleport(getRandomSpawnPoint());
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
        return FFASpawns.get(r.nextInt(FFASpawns.size()));
    }

    @Override
    public void updateSingInfo(Sign s) {
        Game game = GameManager.getInstance().getGame(gameID);

        s.setLine(0, NAME);
        s.setLine(1, game.getState() + "");
        s.setLine(2, game.getActivePlayers() + "/" + game.getMaxPlayer());
        s.setLine(3, "");

    }

    @Override
    public ArrayList<String> updateSignPlayer() {
        Game game = GameManager.getInstance().getGame(gameID);

        ArrayList< String> display = new ArrayList< String>();
        for (Player p : game.getAllPlayers()) {
            display.add((game.isPlayerActive(p) ? ChatColor.BLACK : ChatColor.GRAY) + NameUtil.stylize(p.getName(), true, !game.isPlayerActive(p)));
        }

        return display;
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

    @Override
    public int getMaxPlayer() {
        return (Integer) settings.get(SettingsManager.OptionFlag.FFAMAXP);
    }

    @Override
    public int getMinPlayer() {
        return 0;
    }

    @Override
    public String toString() {
        return "{name:" + NAME + ", gameID:" + gameID + "}";
    }
}
