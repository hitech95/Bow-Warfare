package it.kytech.bowwarfare.gametype;

import it.kytech.bowwarfare.BowWarfare;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.MessageManager.PrefixType;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.SpawnManager;
import it.kytech.bowwarfare.util.NameUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import it.kytech.bowwarfare.util.bossbar.BarAPI;
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

public class FreeForAll implements Gametype {

    public static final String NAME = "FFA";
    public static final String LONG_NAME = "Free For All";

    private int gameID;
    private Game game;

    private boolean isTest = false;

    private ArrayList<Location> FFASpawns;

    private ArrayList<Material> allowedPlace = new ArrayList<Material>();
    private ArrayList<Material> allowedBreak = new ArrayList<Material>();

    private HashMap<Player, Integer> kills = new HashMap<Player, Integer>();

    private HashMap<Block, Player> mines = new HashMap<Block, Player>();

    private HashMap<SettingsManager.OptionFlag, Object> settings = new HashMap<SettingsManager.OptionFlag, Object>();
    private Random r = new Random();

    private MessageManager msgmgr = MessageManager.getInstance();
    private ScoreboardManager sbManager = Bukkit.getScoreboardManager();

    private Scoreboard scoreBoard = sbManager.getNewScoreboard();

    public FreeForAll(Game g) {
        isTest = false;

        this.game = g;
        this.gameID = game.getID();

        FFASpawns = SpawnManager.getInstance().loadSpawns(gameID, NAME);
        loadSettings();

        Objective objective = scoreBoard.registerNewObjective(gameID + "." + NAME + "." + "kill", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(SettingsManager.getInstance().getMessageConfig().getString("messages.scoreboard.scoreboard"));
    }

    public FreeForAll(Game g, boolean isTest) {
        this(g);

        if (isTest) {
            FFASpawns = null;
            kills = null;
            allowedPlace = null;
            allowedBreak = null;
            msgmgr = null;
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
        settings.put(SettingsManager.OptionFlag.FFAMAXP, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".maxp"));
        settings.put(SettingsManager.OptionFlag.FFAKILL, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".kill"));

        saveConfig();
    }

    private void saveConfig() {
        SettingsManager.getInstance().saveGameSettings(settings, gameID);
    }

    @Override
    public boolean onJoin(Player player) {
        msgFall(PrefixType.INFO, "game.playerjoingame", "player-" + player.getName(), "activeplayers-" + game.getActivePlayers(), "maxplayers-" + getMaxPlayer());
        player.teleport(getRandomSpawnPoint());

        BarAPI.setMessage(player, buildBossString(LONG_NAME));
        buildScoreBoard(player);

        updateScoreBoard();

        msgmgr.sendFMessage(PrefixType.INFO, "gametype.FFA", player);

        if (game.getState() != Game.GameState.INGAME) {
            game.startGame();
        }

        return true;
    }

    @Override
    public boolean onPlayerKilled(Player victim, final Player killer, boolean hasLeft) {
        if (!hasLeft) {

            if (killer == null) {
                return false;
            }

            if (kills.get(killer) == null) {
                kills.put(killer, 0);
            }
            int kill = kills.get(killer) + 1;

            kills.put(killer, kill);

            Objective objective = scoreBoard.getObjective(gameID + "." + NAME + "." + "kill");
            Score score = objective.getScore(killer.getName());
            score.setScore(kill);

            victim.teleport(getRandomSpawnPoint());
        } else {
            scoreBoard.resetScores(victim.getName());
        }
        return true;
    }

    @Override
    public void checkWin(Player victim, final Player killer) {
        int kill = kills.get(killer);

        if (kill >= (Integer) settings.get(SettingsManager.OptionFlag.FFAKILL)) {
            game.playerWin(victim, killer);
            BarAPI.setMessage(killer, buildBossString(SettingsManager.getInstance().getMessageConfig().getString("messages.game.winner", "You are the Winner! "))); //Blank space to fix visual error!

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("BowWarfare-Reloaded"), new Runnable() {

                @Override
                public void run() {
                    BarAPI.removeBar(killer);
                }

            }, 10 * 20);
        } else {
            if ((kill % 5) == 0 || kill >= ((Integer) settings.get(SettingsManager.OptionFlag.FFAKILL) - 5)) {
                msgFall(PrefixType.INFO, "kill.missing",
                        "player-" + (BowWarfare.auth.contains(killer) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "") + killer.getName(),
                        "kill-" + (((Integer) settings.get(SettingsManager.OptionFlag.FFAKILL)) - kill)
                );
            }
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
        return FFASpawns.size();
    }

    @Override
    public Location getRandomSpawnPoint() {
        return FFASpawns.get(r.nextInt(FFASpawns.size()));
    }

    @Override
    public void updateSingInfo(Sign s) {
        s.setLine(0, NAME);
        s.setLine(1, game.getState() + "");
        s.setLine(2, game.getActivePlayers() + "/" + game.getMaxPlayer());
        s.setLine(3, "");
    }

    @Override
    public ArrayList<String> updateSignPlayer() {
        ArrayList<String> display = new ArrayList<String>();
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
        return allowedBreak.contains(block.getType());

    }

    @Override
    public boolean onBlockPlaced(Block block, Player p) {
        if (block.getType() == Material.IRON_PLATE || block.getType() == Material.GOLD_PLATE) {
            mines.put(block, p);
            return true;
        }
        return allowedPlace.contains(block.getType());
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
        return false;
    }

    @Override
    public boolean tryLoadSpawn() {
        return (SpawnManager.getInstance().getNumberOf(gameID, NAME) > 0);
    }

    @Override
    public void addSpawn(Location l, String... args) {
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
        Objective objective = scoreBoard.getObjective(gameID + "." + NAME + "." + "kill");
        Score score = objective.getScore(player.getName());
        score.setScore(0);
        player.setScoreboard(scoreBoard);
    }

    private void updateScoreBoard() {
        for (Player p : game.getAllPlayers()) {
            if (game.isPlayerActive(p)) {
                p.setScoreboard(scoreBoard);
            }
        }
    }

    public void msgFall(PrefixType type, String msg, String... vars) {
        for (Player p : game.getAllPlayers()) {
            msgmgr.sendFMessage(type, msg, p, vars);
        }
    }

    @Override
    public boolean requireVote() {
        return false;
    }

    @Override
    public String toString() {
        return "{name:" + NAME + ", longName:" + LONG_NAME + ", gameID:" + gameID + "}";
    }
}
