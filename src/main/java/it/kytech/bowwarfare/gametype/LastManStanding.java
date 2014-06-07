package it.kytech.bowwarfare.gametype;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.Game.GameState;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.MessageManager.PrefixType;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.SpawnManager;
import it.kytech.bowwarfare.util.NameUtil;
import it.kytech.bowwarfare.util.bossbar.StatusBarAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class LastManStanding implements Gametype {

    public static final String NAME = "LMS";
    public static final String LONG_NAME = "Last Man Standing";

    private int gameID;
    private Game game;

    private boolean isTest = false;

    private ArrayList<Location> LMSSpawns;
    private HashMap<Integer, Player> firstFreeSpawn = new HashMap<Integer, Player>();

    private ArrayList<Integer> allowedPlace = new ArrayList<Integer>();
    private ArrayList<Integer> allowedBreak = new ArrayList<Integer>();

    private HashMap<Player, Integer> life = new HashMap<Player, Integer>();

    private HashMap<Block, Player> mines = new HashMap<Block, Player>();

    private HashMap<SettingsManager.OptionFlag, Object> settings = new HashMap<SettingsManager.OptionFlag, Object>();
    private Random r = new Random();

    private MessageManager msgmgr = MessageManager.getInstance();
    private ScoreboardManager sbManager = Bukkit.getScoreboardManager();

    private Scoreboard scoreBoard = sbManager.getNewScoreboard();

    public LastManStanding(Game g) {
        isTest = false;

        this.game = g;
        this.gameID = game.getID();

        loadSettings();

        LMSSpawns = SpawnManager.getInstance().loadSpawns(gameID, NAME, "");
        for (int a = 1; a <= LMSSpawns.size(); a++) {
            firstFreeSpawn.put(a, null);
        }

        Objective objective = scoreBoard.registerNewObjective(gameID + "." + NAME + "." + "life", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(SettingsManager.getInstance().getMessageConfig().getString("messages.scoreboard.life"));
    }

    public LastManStanding(Game g, boolean isTest) {
        this(g);
        this.isTest = isTest;

        if (isTest) {
            LMSSpawns = null;
            allowedPlace = null;
            allowedBreak = null;
            msgmgr = null;
            life = null;
            mines = null;
            sbManager = null;
            scoreBoard = null;
            r = null;
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
        settings.put(SettingsManager.OptionFlag.LMSLIFE, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".life"));
        settings.put(SettingsManager.OptionFlag.LMSMINP, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".minp"));
        settings.put(SettingsManager.OptionFlag.LMSTIME, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".time"));

        saveConfig();
    }

    private void saveConfig() {
        SettingsManager.getInstance().saveGameSettings(settings, gameID);
    }

    @Override
    public boolean onJoin(Player player) {

        if (game.getGameState() != GameState.WAITING) {
            return false;
        }

        player.teleport(LMSSpawns.get(firstFreeSpawn()));

        firstFreeSpawn.put(firstFreeSpawn(), player);
        life.put(player, (Integer) settings.get(SettingsManager.OptionFlag.LMSLIFE));

        StatusBarAPI.setStatusBar(player, buildBossString(LONG_NAME), 1);
        buildScoreBoard(player);

        updateScoreBoard();

        msgmgr.sendFMessage(MessageManager.PrefixType.INFO, "gametype.LMS", player);

        //TODO - Verify the number of presence and make percentage
        //Make the percentage to auto-start the game, etc
        //TODO - Only for Test REMOVE THIS
        game.startGame();

        return true;
    }

    @Override
    public boolean onPlayerKilled(Player victim, Player killer, boolean hasLeft) {
        if (!hasLeft) {

            if (killer == null) {
                return false;
            }

            int lifeCount = life.get(victim) - 1;
            life.put(victim, lifeCount);

            if (lifeCount == 0) {
                game.clearInv(victim);

                StatusBarAPI.removeStatusBar(victim);
                victim.setScoreboard(sbManager.getNewScoreboard());

                game.markAsInactive(victim);
                game.restoreInv(victim);

                //TODO not left arena but loose the match
                msgFall(MessageManager.PrefixType.INFO, "game.playerloosegame", "player-" + victim.getName());
                
                game.addSpectator(victim);
            } else {
                victim.teleport(getRandomSpawnPoint());
            }

            Objective objective = scoreBoard.getObjective(gameID + "." + NAME + "." + "life");
            Score score = objective.getScore(victim);
            score.setScore(lifeCount);
        } else {
            scoreBoard.resetScores(victim);
        }
        return true;
    }

    @Override
    public void checkWin(Player victim, final Player killer) {
        if (game.getActivePlayers() < 2) {

            game.playerWin(victim, killer);

            StatusBarAPI.setStatusBar(killer, buildBossString(SettingsManager.getInstance().getMessageConfig().getString("messages.game.winner", "You are the Winner! ")), 1); //Blank space to fix visual error!

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("BowWarfare-Reloaded"), new Runnable() {
                @Override
                public void run() {
                    StatusBarAPI.removeStatusBar(killer);
                }
            }, 10 * 20);
        }
    }

    @Override
    public boolean onPlayerRemove(Player player, boolean hasLeft) {
        return false;
    }

    @Override
    public boolean onPlayerQuit(Player p) {
        return false;
    }

    @Override
    public boolean onProjectileHit(Player attacker, Projectile pro) {
        if (pro instanceof Snowball) {
            Snowball snowball = (Snowball) pro;
            Location loc = snowball.getLocation();

            for (Player other : game.getAllPlayers()) {
                if (other.getLocation().distance(loc) <= 4 && game.isPlayerActive(other) && other != attacker) {
                    other.setLastDamageCause(new EntityDamageByEntityEvent(snowball, other, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, other.getHealth()));
                    game.killPlayer(other, attacker);
                }
            }

            loc.getWorld().createExplosion(loc, 0);

            return true;
        }
        return false;
    }

    @Override
    public String getGametypeName() {
        return NAME;
    }

    @Override
    public int getSpawnCount(String... args) {
        return LMSSpawns.size();
    }

    @Override
    public Location getRandomSpawnPoint() {
        return LMSSpawns.get(r.nextInt(LMSSpawns.size()));
    }

    @Override
    public void updateSingInfo(Sign s) {
        s.setLine(0, NAME);
        s.setLine(1, game.getState() + "");
        s.setLine(2, game.getActivePlayers() + "/" + game.getInactivePlayers() + "/" + game.getMaxPlayer());
        s.setLine(3, "");
    }

    @Override
    public ArrayList<String> updateSignPlayer() {
        ArrayList< String> display = new ArrayList< String>();
        for (Player p : game.getAllPlayers()) {
            display.add((game.isPlayerActive(p) ? ChatColor.BLACK : ChatColor.GRAY) + NameUtil.stylize(p.getName(), true, !game.isPlayerActive(p)));
        }
        return display;
    }

    @Override
    public boolean onBlockBreaked(Block block, Player p) {
        if (block.getType() == Material.IRON_PLATE || block.getType() == Material.GOLD_PLATE) {
            if (mines.containsKey(block)) {
                mines.remove(block);
                return true;
            }
        }
        return allowedBreak.contains(block.getTypeId());

    }

    @Override
    public boolean onBlockPlaced(Block block, Player p) {
        if (block.getType() == Material.IRON_PLATE || block.getType() == Material.GOLD_PLATE) {
            mines.put(block, p);
            return true;
        }
        return allowedPlace.contains(block.getTypeId());
    }

    @Override
    public boolean onBlockInteract(Block block, Player p) {
        if (block.getType() == Material.IRON_PLATE || block.getType() == Material.GOLD_PLATE) {
            Player killer = mines.get(block);

            if (p == killer || killer == null) {
                return false;
            }

            for (Player other : game.getAllPlayers()) {
                if (other.getLocation().distance(block.getLocation()) <= 4 && game.isPlayerActive(other) && other != killer) {
                    other.setLastDamageCause(new EntityDamageByBlockEvent(block, other, EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, other.getHealth()));
                    game.killPlayer(other, killer);
                }
            }

            block.getWorld().createExplosion(block.getLocation(), 0);

            mines.remove(block);
            block.setType(Material.AIR);
            return true;
        }
        return true;
    }

    @Override
    public boolean isFrozenSpawn() {
        if (game.getState() == Game.GameState.INGAME) {
            return false;
        }
        return true;
    }

    @Override
    public boolean tryLoadSpawn() {
        return (SpawnManager.getInstance().getNumberOf(gameID, NAME) > 0);
    }

    @Override
    public void addSpawn(Location l, String... args) {
        firstFreeSpawn.put(LMSSpawns.size(), null);
        LMSSpawns.add(l);
    }

    @Override
    public int getMaxPlayer() {
        return getSpawnCount();
    }

    @Override
    public int getMinPlayer() {
        return (Integer) settings.get(SettingsManager.OptionFlag.LMSMINP);
    }

    private String buildBossString(String s) {
        int length = s.length();
        int left = (64 - length) / 2;
        int right = (64 - length) / 2;

        StringBuilder str = new StringBuilder();

        for (int i = 0; i < left; i++) {
            str.append(" ");
        }

        str.append(s);

        for (int i = 0; i < right; i++) {
            str.append(" ");
        }

        return str.toString();
    }

    private void buildScoreBoard(Player player) {
        Objective objective = scoreBoard.getObjective(gameID + "." + NAME + "." + "life");
        Score score = objective.getScore(player);
        score.setScore((Integer) settings.get(SettingsManager.OptionFlag.LMSLIFE));
        player.setScoreboard(scoreBoard);
    }

    private void updateScoreBoard() {
        for (Player p : game.getAllPlayers()) {
            if (game.isPlayerActive(p)) {
                p.setScoreboard(scoreBoard);
            }
        }
    }

    private int firstFreeSpawn() {
        for (int a = 1; a <= getSpawnCount(); a++) {
            if (firstFreeSpawn.get(a) == null) {
                return a;
            }
        }
        return -1;
    }

    public void msgFall(PrefixType type, String msg, String... vars) {
        for (Player p : game.getAllPlayers()) {
            msgmgr.sendFMessage(type, msg, p, vars);
        }
    }

    @Override
    public String toString() {
        return "{name:" + NAME + ", longName:" + LONG_NAME + ", gameID:" + gameID + "}";
    }
}
