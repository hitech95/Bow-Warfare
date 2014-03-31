/*
 * Copyright (C) 2014 M2K
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.kytech.bowwarfare.gametype;

import it.kytech.bowwarfare.BowWarfare;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
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
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

/**
 *
 * @author M2K
 */
public class TeamDeathMatch implements Gametype {

    public static final String NAME = "TDM";
    public static final String LONG_NAME = "Team Death Match";

    private int gameID;
    private boolean isTest = false;

    private ArrayList<Location> redSpawns;
    private ArrayList<Location> blueSpawns;

    private ArrayList<Player> redTeam = new ArrayList<Player>();
    private ArrayList<Player> blueTeam = new ArrayList<Player>();

    private ArrayList<Integer> allowedPlace = new ArrayList<Integer>();
    private ArrayList<Integer> allowedBreak = new ArrayList<Integer>();

    private HashMap<Teams, Integer> kills = new HashMap<Teams, Integer>();

    private MessageManager msgmgr = MessageManager.getInstance();
    private HashMap<SettingsManager.OptionFlag, Object> settings = new HashMap<SettingsManager.OptionFlag, Object>();
    private HashMap<Block, Player> mines = new HashMap<Block, Player>();

    private ScoreboardManager sbManager = Bukkit.getScoreboardManager();

    private Scoreboard redScoreBoard = sbManager.getNewScoreboard();
    private Scoreboard blueScoreBoard = sbManager.getNewScoreboard();

    private Random r = new Random();

    private final int DEFAULT_MAXP = 20;
    private final int DEFAULT_KILL = 60;

    private static enum Teams {

        RED, BLUE
    }

    public TeamDeathMatch(int gameID) {
        isTest = false;
        this.gameID = gameID;

        redSpawns = SpawnManager.getInstance().loadSpawns(gameID, NAME, "red");
        blueSpawns = SpawnManager.getInstance().loadSpawns(gameID, NAME, "blue");

        kills.put(Teams.RED, 0);
        kills.put(Teams.BLUE, 0);

        loadSettings();

        Objective redObjective = redScoreBoard.registerNewObjective(gameID + "." + NAME + "." + "RED." + "kill", "dummy");
        Objective blueObjective = blueScoreBoard.registerNewObjective(gameID + "." + NAME + "." + "BLUE." + "kill", "dummy");

        redObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        blueObjective.setDisplaySlot(DisplaySlot.SIDEBAR);

        redObjective.setDisplayName("ScoreBoard");
        blueObjective.setDisplayName("ScoreBoard");
    }

    public TeamDeathMatch(int gameID, boolean isTest) {
        this(gameID);

        if (isTest) {
            redSpawns = null;
            blueSpawns = null;
            redTeam = null;
            blueTeam = null;
            kills = null;
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
        settings.put(SettingsManager.OptionFlag.TDMMAXP, DEFAULT_MAXP);
        settings.put(SettingsManager.OptionFlag.TDMKILL, DEFAULT_KILL);

        saveConfig();
    }

    private void saveConfig() {
        SettingsManager.getInstance().saveGameSettings(settings, gameID);
    }

    @Override
    public boolean onJoin(Player player) {
        Teams t = balanceNewPlayer();
        player.teleport(getRandomSpawnPoint(t));

        if (t == Teams.RED) {
            redTeam.add(player);
        } else {
            blueTeam.add(player);
        }

        StatusBarAPI.setStatusBar(player, buildBossStringTDM(), 1);
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
        Game game = GameManager.getInstance().getGame(gameID);
        if (!hasLeft) {

            int kill = kills.get(getTeam(killer)) + 1;

            if (kill >= (Integer) settings.get(SettingsManager.OptionFlag.TDMKILL)) {

                game.playerWin(victim, killer);

                for (Player p : (getTeam(killer) == Teams.RED) ? redTeam : blueTeam) {
                    final Player pf = p;

                    StatusBarAPI.setStatusBar(p, buildBossString(SettingsManager.getInstance().getMessageConfig().getString("messages.game.team.winner", "Your Team have Won! ")), 1); //Blank space to fix visual error!

                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("BowWarfare-Reloaded"), new Runnable() {

                        @Override
                        public void run() {
                            StatusBarAPI.removeStatusBar(pf);
                        }

                    }, 10 * 20);

                }
                return true;
            } else {
                if ((kill % 5) == 0 || kill >= ((Integer) settings.get(SettingsManager.OptionFlag.TDMKILL) - 5)) {
                    msgmgr.sendFMessage(MessageManager.PrefixType.INFO, "kill.missing", killer,
                            "player-" + (BowWarfare.auth.contains(killer) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "") + killer.getName(),
                            "kill-" + (((Integer) settings.get(SettingsManager.OptionFlag.TDMKILL)) - kill)
                    );
                }
                kills.put(getTeam(killer), kill);

                Scoreboard scoreBoard = (getTeam(killer) == Teams.RED) ? redScoreBoard : blueScoreBoard;
                Objective objective = scoreBoard.getObjective(gameID + "." + NAME + "." + getTeam(killer).toString() + "." + "kill");
                Score score = objective.getScore(killer);
                score.setScore(kill);

                updateAllBossStringTDM();
            }

            victim.teleport(getRandomSpawnPoint(getTeam(victim)));

        } else {
            if (getTeam(killer) == Teams.RED) {
                redScoreBoard.resetScores(victim);
                redTeam.remove(victim);
            } else {
                blueScoreBoard.resetScores(victim);
                blueTeam.remove(victim);
            }
        }

        return true;
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
    public boolean onArrowHit(Player attacker, Arrow arrow) {
        return false;
    }

    @Override
    public String getGametypeName() {
        return NAME;
    }

    @Override
    public int getSpawnCount(String... args) {
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

    public Location getRandomSpawnPoint(Teams t) {
        if (t == Teams.RED) {
            return redSpawns.get(r.nextInt(redSpawns.size()));
        } else {
            return blueSpawns.get(r.nextInt(blueSpawns.size()));
        }
    }

    @Override
    public void updateSingInfo(Sign s) {
        Game game = GameManager.getInstance().getGame(gameID);

        s.setLine(0, NAME);
        s.setLine(1, game.getState() + "");
        s.setLine(2, game.getActivePlayers() + "/" + ChatColor.RED + redTeam.size() + ChatColor.BLACK + "/" + ChatColor.BLUE + blueTeam.size());
        s.setLine(3, "");

    }

    @Override
    public ArrayList<String> updateSignPlayer() {
        Game game = GameManager.getInstance().getGame(gameID);

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
            Game game = GameManager.getInstance().getGame(gameID);
            Player killer = mines.get(block);

            if (p == killer) {
                return false;
            }

            for (Player other : game.getAllPlayers()) {
                if (other.getLocation().distance(block.getLocation()) <= 4 && game.isPlayerActive(other)) {
                    System.out.println("Deve morire!");
                    game.killPlayer(other, killer);
                }
            }

            block.getWorld().createExplosion(block.getLocation(), 0);
            mines.remove(block);

            block.setType(Material.AIR);
            return true;
        }
        return allowedPlace.contains(block.getTypeId());
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
        Game g = GameManager.getInstance().getGame(gameID);
        String s = buildBossStringTDM();
        for (Player p : g.getAllPlayers()) {
            if (g.isPlayerActive(p)) {
                StatusBarAPI.setStatusBar(p, s, 1);
            }
        }
    }

    private void buildScoreBoard(Player player) {
        Scoreboard scoreBoard = (getTeam(player) == Teams.RED) ? redScoreBoard : blueScoreBoard;

        Objective objective = scoreBoard.getObjective(gameID + "." + NAME + "." + getTeam(player).toString() + "." + "kill");

        Score score = objective.getScore(player);
        score.setScore(0);
        player.setScoreboard(scoreBoard);
    }

    private void updateScoreBoard() {
        Game game = GameManager.getInstance().getGame(gameID);

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

    @Override
    public String toString() {
        return "{name:" + NAME + ", longName:" + LONG_NAME + ", gameID:" + gameID + "}";
    }
}
