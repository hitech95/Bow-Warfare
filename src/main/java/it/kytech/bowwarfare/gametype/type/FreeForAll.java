package it.kytech.bowwarfare.gametype.type;

import it.kytech.bowwarfare.BowWarfare;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.gametype.DummyGametype;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;
import it.kytech.bowwarfare.manager.SettingsManager;
import it.kytech.bowwarfare.manager.SpawnManager;
import java.util.ArrayList;
import it.kytech.bowwarfare.util.bossbar.BarAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class FreeForAll extends DummyGametype {

    public static String NAME = "FFA";
    public static String LONG_NAME = "Free For All";

    private ArrayList<Location> FFASpawns;

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

    protected void loadDefaultSettings() {
        settings.put(SettingsManager.OptionFlag.FFAMAXP, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".maxp"));
        settings.put(SettingsManager.OptionFlag.FFAKILL, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".kill"));

        saveConfig();
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
                        "player-" + (BowWarfare.auth.contains(killer.getName()) ? ChatColor.DARK_RED + "" + ChatColor.BOLD : "") + killer.getName(),
                        "kill-" + (((Integer) settings.get(SettingsManager.OptionFlag.FFAKILL)) - kill)
                );
            }
        }
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
}
