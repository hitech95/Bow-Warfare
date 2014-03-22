package it.kytech.bowwarfare.gametype;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.SpawnManager;
import static it.kytech.bowwarfare.gametype.FreeForAll.NAME;
import it.kytech.bowwarfare.util.NameUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

/**
 *
 * @author M2K
 */
public class LastManStanding implements Gametype {

    public static final String NAME = "LMS";
    public static final String LONG_NAME = "Last ManS tanding";

    private int gameID;
    private boolean isTest = false;
    private ArrayList<Location> LMSSpawns;
    private ArrayList<Integer> allowedPlace = new ArrayList<Integer>();
    private ArrayList<Integer> allowedBreak = new ArrayList<Integer>();
    private HashMap<Player, Integer> kills = new HashMap<Player, Integer>();
    private MessageManager msgmgr = MessageManager.getInstance();
    private HashMap<SettingsManager.OptionFlag, Object> settings = new HashMap<SettingsManager.OptionFlag, Object>();
    private Random r = new Random();

    private final int DEFAULT_MAXP = 16;

    public LastManStanding(int gameID) {
        isTest = false;
        this.gameID = gameID;

        LMSSpawns = SpawnManager.getInstance().loadSpawns(gameID, NAME, "");
        loadSettings();
    }

    public LastManStanding(int gameID, boolean isTest) {
        this.gameID = gameID;
        this.isTest = isTest;

        if (isTest) {
            LMSSpawns = null;
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
        settings.put(SettingsManager.OptionFlag.LMSMAXP, DEFAULT_MAXP);

        saveConfig();
    }

    private void saveConfig() {
        SettingsManager.getInstance().saveGameSettings(settings, gameID);
    }

    @Override
    public boolean onPlayerRemove(Player player, boolean hasLeft) {
        //Mark Player as Death
        // set in spectator mode        
        return true;
    }

    @Override
    public boolean onPlayerQuit(Player p) {
        //Mark Player as Death
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
        return LMSSpawns.size();
    }

    @Override
    public Location getRandomSpawnPoint() {
        return LMSSpawns.get(r.nextInt(LMSSpawns.size()));
    }

    @Override
    public void updateSingInfo(Sign s) {
        Game game = GameManager.getInstance().getGame(gameID);

        s.setLine(0, NAME);
        s.setLine(1, game.getState() + "");
        s.setLine(2, game.getActivePlayers() + "/" + game.getInactivePlayers() + "/" + game.getMaxPlayer());
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
        if (GameManager.getInstance().getGame(gameID).getState() == Game.GameState.INGAME) {
            return true;
        }
        return false;
    }

    @Override
    public boolean tryLoadSpawn() {
        return (SpawnManager.getInstance().getNumberOf(gameID, NAME) > 0);
    }

    @Override
    public void addSpawn(Location l, String... args) {
        LMSSpawns.add(l);
    }

    @Override
    public int getMaxPlayer() {
        return (Integer) settings.get(SettingsManager.OptionFlag.LMSMAXP);
    }

    @Override
    public int getMinPlayer() {
        return 0;
    }

    @Override
    public String toString() {
        return "{name:" + NAME + ", gameID:" + gameID + "}";
    }

    @Override
    public boolean onJoin(Player player) {
        player.teleport(getRandomSpawnPoint());

        //Make the percentage to auto-start the game, etc.
        return true;
    }

    @Override
    public boolean onPlayerKilled(Player victim, Player killer, boolean hasLeft) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean onBlockInteract(Block block, Player p) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
