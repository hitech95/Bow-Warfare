package it.kytech.bowwarfare.gametype;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.manager.EconomyManager;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.SettingsManager;
import it.kytech.bowwarfare.manager.SpawnManager;
import it.kytech.bowwarfare.util.NameUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import it.kytech.bowwarfare.util.bossbar.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
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
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class TeamDeathMatch implements IGametype {

    public static final String NAME = "TDM";
    public static final String LONG_NAME = "Team Death Match";

    private int gameID;
    private Game game;

    private boolean isTest = false;

    private ArrayList<Location> redSpawns;
    private ArrayList<Location> blueSpawns;

    private ArrayList<Player> redTeam = new ArrayList<Player>();
    private ArrayList<Player> blueTeam = new ArrayList<Player>();

    private ArrayList<Material> allowedPlace = new ArrayList<Material>();
    private ArrayList<Material> allowedBreak = new ArrayList<Material>();

    private HashMap<Teams, Integer> kills = new HashMap<Teams, Integer>();

    private MessageManager msgmgr = MessageManager.getInstance();

    private HashMap<SettingsManager.OptionFlag, Object> settings = new HashMap<SettingsManager.OptionFlag, Object>();
    private HashMap<Block, Player> mines = new HashMap<Block, Player>();

    private ScoreboardManager sbManager = Bukkit.getScoreboardManager();

    private Scoreboard redScoreBoard = sbManager.getNewScoreboard();
    private Scoreboard blueScoreBoard = sbManager.getNewScoreboard();

    private Random r = new Random();

    private static enum Teams {

        RED, BLUE
    }

    public TeamDeathMatch(Game game) {
        this.game = game;
        this.gameID = game.getID();

        isTest = false;

        redSpawns = SpawnManager.getInstance().loadSpawns(gameID, NAME, "red");
        blueSpawns = SpawnManager.getInstance().loadSpawns(gameID, NAME, "blue");

        kills.put(Teams.RED, 0);
        kills.put(Teams.BLUE, 0);

        loadSettings();

        Objective redObjective = redScoreBoard.registerNewObjective(gameID + "." + NAME + ".RED." + "kill", "dummy");
        Objective blueObjective = blueScoreBoard.registerNewObjective(gameID + "." + NAME + ".BLUE." + "kill", "dummy");

        redObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        blueObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        redObjective.setDisplayName(ChatColor.RED + SettingsManager.getInstance().getMessageConfig().getString("messages.scoreboard.scoreboard"));
        blueObjective.setDisplayName(ChatColor.BLUE + SettingsManager.getInstance().getMessageConfig().getString("messages.scoreboard.scoreboard"));
    }

    public TeamDeathMatch(Game g, boolean isTest) {
        this(g);

        if (isTest) {
            redSpawns = null;
            blueSpawns = null;
            redTeam = null;
            blueTeam = null;
            kills = null;
            allowedPlace = null;
            allowedBreak = null;
            msgmgr = null;
            mines = null;
            sbManager = null;
            redScoreBoard = null;
            blueScoreBoard = null;
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
        settings.put(SettingsManager.OptionFlag.TDMMAXP, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".maxp"));
        settings.put(SettingsManager.OptionFlag.TDMKILL, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".kill"));

        saveConfig();
    }

    private void saveConfig() {
        SettingsManager.getInstance().saveGameSettings(settings, gameID);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onJoin(Player player) {
        Teams t = balanceNewPlayer();

        msgFall(MessageManager.PrefixType.INFO, "game.team.join", "player-" + player.getName(), "team-" + t.name().toUpperCase(), "teamsplayers-" + ChatColor.RED + redTeam.size() + ChatColor.WHITE + "/" + ChatColor.BLUE + blueTeam.size(), "maxplayers-" + getMaxPlayer());

        player.teleport(getRandomSpawnPoint(t));

        ItemStack coloredWool;

        if (t == Teams.RED) {
            redTeam.add(player);
            coloredWool = new ItemStack(Material.WOOL, 1, DyeColor.RED.getData());
        } else {
            blueTeam.add(player);
            coloredWool = new ItemStack(Material.WOOL, 1, DyeColor.BLUE.getData());
        }

        player.getInventory().setHelmet(coloredWool);
        player.updateInventory();

        BarAPI.setMessage(player, buildBossStringTDM());

        buildScoreBoard(player);
        updateScoreBoard();

        msgmgr.sendFMessage(MessageManager.PrefixType.INFO, "gametype.TDM." + t.toString().toLowerCase(), player);

        if (GameManager.getInstance().getGame(gameID).getState() != Game.GameState.INGAME) {
            GameManager.getInstance().getGame(gameID).startGame();
        }

        return true;
    }

    @Override
    public boolean onPlayerKilled(Player victim, final Player killer, boolean hasLeft) {
        if (!hasLeft) {

            if (getTeam(killer) == getTeam(victim)) {
                return false;
            }

            int kill = 0;
            Teams victimTeam = getTeam(victim);
            Teams killerTeam = null;

            if (killer == null) {
                if (victimTeam == Teams.RED) {
                    killerTeam = Teams.BLUE;
                } else {
                    killerTeam = Teams.RED;
                }
            } else {
                killerTeam = getTeam(killer);
            }

            kill = kills.get(killerTeam) + 1;

            kills.put(getTeam(killer), kill);

            Scoreboard scoreBoard = (getTeam(killer) == Teams.RED) ? redScoreBoard : blueScoreBoard;
            Objective objective = scoreBoard.getObjective(gameID + "." + NAME + "." + killerTeam.name().toUpperCase() + "." + "kill");
            Score score = objective.getScore(killer.getName());
            score.setScore(score.getScore() + 1);

            updateAllBossStringTDM();

            victim.teleport(getRandomSpawnPoint(getTeam(victim)));

        } else {
            if (getTeam(killer) == Teams.RED) {
                redScoreBoard.resetScores(victim.getName());
                redTeam.remove(victim);
            } else {
                blueScoreBoard.resetScores(victim.getName());
                blueTeam.remove(victim);
            }
        }

        return true;
    }

    @Override
    public void checkWin(Player victim, Player killer) {
        if (getTeam(killer) == getTeam(victim)) {
            return;
        }

        int kill = 0;
        Teams victimTeam = getTeam(victim);
        Teams killerTeam = null;

        if (killer == null) {
            if (victimTeam == Teams.RED) {
                killerTeam = Teams.BLUE;
            } else {
                killerTeam = Teams.RED;
            }
        } else {
            killerTeam = getTeam(killer);
        }

        kill = kills.get(killerTeam);

        if (kill >= (Integer) settings.get(SettingsManager.OptionFlag.TDMKILL)) {

            game.playerWin(victim, killer);

            for (Player p : (getTeam(killer) == Teams.RED) ? redTeam : blueTeam) {
                final Player pf = p;

                BarAPI.setMessage(p, buildBossString(SettingsManager.getInstance().getMessageConfig().getString("messages.game.team.winner", "Your Team have Won! "))); //Blank space to fix visual error!
                EconomyManager.getInstance().executeTask(EconomyManager.win, p);

                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("BowWarfare-Reloaded"), new Runnable() {
                    @Override
                    public void run() {
                        BarAPI.removeBar(pf);
                    }

                }, 10 * 20);
            }

            for (Player p : (getTeam(killer) == Teams.RED) ? blueTeam : redTeam) {
                if (game.isPlayerActive(p) && !p.equals(killer)) {
                    EconomyManager.getInstance().executeTask(EconomyManager.loose, p);
                }
            }
        } else {
            if ((kill % 10) == 0 || kill >= ((Integer) settings.get(SettingsManager.OptionFlag.TDMKILL) - 5)) {
                msgFall(MessageManager.PrefixType.INFO, "kill.team.missing",
                        "team-" + getTeam(killer).name().toUpperCase(),
                        "kill-" + (((Integer) settings.get(SettingsManager.OptionFlag.TDMKILL)) - kill)
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
                if (other.getLocation().distance(loc) <= 4 && game.isPlayerActive(other)) {
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
    public int getSpawnCount(String... args
    ) {
        if (args.length < 1) {
            return redSpawns.size() + blueSpawns.size();
        }

        if (args[0].equalsIgnoreCase("red")) {
            return redSpawns.size();
        }

        if (args[0].equalsIgnoreCase("blue")) {
            return blueSpawns.size();
        }

        return 0;

    }

    @Override
    public Location getRandomSpawnPoint() {
        if (r.nextBoolean()) {
            return redSpawns.get(r.nextInt(redSpawns.size()));
        } else {
            return blueSpawns.get(r.nextInt(blueSpawns.size()));
        }
    }

    private Location getRandomSpawnPoint(Teams t) {
        if (t == Teams.RED) {
            return redSpawns.get(r.nextInt(redSpawns.size()));
        } else {
            return blueSpawns.get(r.nextInt(blueSpawns.size()));
        }
    }

    @Override
    public void updateSingInfo(Sign s) {
        s.setLine(0, NAME);
        s.setLine(1, game.getState() + "");
        s.setLine(2, game.getActivePlayers() + "/" + ChatColor.RED + redTeam.size() + ChatColor.BLACK + "/" + ChatColor.BLUE + blueTeam.size());
        s.setLine(3, "");
    }

    @Override
    public ArrayList<String> updateSignPlayer() {
        ArrayList<String> display = new ArrayList<String>();
        for (Player p : game.getAllPlayers()) {
            if (game.isPlayerActive(p)) {
                display.add((getTeam(p) == Teams.RED ? ChatColor.RED : ChatColor.BLUE) + NameUtil.stylize(p.getName(), true, false));
            } else {
                display.add(ChatColor.GRAY + NameUtil.stylize(p.getName(), true, true));
            }

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

            if (p == killer || getTeam(p) == getTeam(killer) || killer == null) {
                return false;
            }

            for (Player other : game.getAllPlayers()) {
                if (other.getLocation().distance(block.getLocation()) <= 4 && game.isPlayerActive(other)) {
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
    public boolean onGameStart() {
        return false;
    }

    @Override
    public boolean isFrozenSpawn() {
        return false;
    }

    @Override
    public boolean tryLoadSpawn() {
        return (SpawnManager.getInstance().getNumberOf(gameID, NAME, "red") > 0 && SpawnManager.getInstance().getNumberOf(gameID, NAME, "blue") > 0);
    }

    @Override
    public void addSpawn(Location l, String... args) {
        if (Teams.valueOf(args[0].toUpperCase()) == Teams.RED) {
            redSpawns.add(l);
        } else if (Teams.valueOf(args[0].toUpperCase()) == Teams.BLUE) {
            blueSpawns.add(l);
        }
    }

    @Override
    public int getMaxPlayer() {
        return (Integer) settings.get(SettingsManager.OptionFlag.TDMMAXP);
    }

    @Override
    public int getMinPlayer() {
        return 0;
    }

    private Teams getTeam(Player p) {
        if (redTeam.contains(p)) {
            return Teams.RED;
        } else if (blueTeam.contains(p)) {
            return Teams.BLUE;
        }
        return null;
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

    private String buildBossStringTDM() {
        String redKillStr = "" + kills.get(Teams.RED);
        String blueKillStr = "" + kills.get(Teams.BLUE);

        String s = ChatColor.RED + redKillStr + "          " + ChatColor.WHITE + LONG_NAME + "          " + ChatColor.BLUE + blueKillStr;

        int left = (64 - s.length()) / 2;
        int right = 64 - left - s.length();

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

    private void updateAllBossStringTDM() {
        String s = buildBossStringTDM();
        for (Player p : game.getAllPlayers()) {
            if (game.isPlayerActive(p)) {
                BarAPI.setMessage(p, s);
            }
        }
    }

    private void buildScoreBoard(Player player) {
        Scoreboard scoreBoard = (getTeam(player) == Teams.RED) ? redScoreBoard : blueScoreBoard;

        Objective objective = scoreBoard.getObjective(gameID + "." + NAME + "." + getTeam(player).name().toUpperCase() + "." + "kill");

        Score score = objective.getScore(player.getName());
        score.setScore(0);
        player.setScoreboard(scoreBoard);
    }

    private void updateScoreBoard() {
        for (Player p : game.getAllPlayers()) {
            if (game.isPlayerActive(p)) {
                Scoreboard scoreBoard = (getTeam(p) == Teams.RED) ? redScoreBoard : blueScoreBoard;
                p.setScoreboard(scoreBoard);
            }
        }
    }

    public Teams balanceNewPlayer() {
        if (redTeam.size() < blueTeam.size()) {
            return Teams.RED;
        } else if (redTeam.size() > blueTeam.size()) {
            return Teams.BLUE;
        } else {
            return ((Integer) kills.get(Teams.RED) < (Integer) kills.get(Teams.BLUE)) ? Teams.RED : Teams.BLUE;
        }
    }

    public void msgFall(MessageManager.PrefixType type, String msg, String... vars) {
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
