package it.kytech.bowwarfare.gametype.type;

import it.kytech.bowwarfare.BowWarfare;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.Game.GameState;
import it.kytech.bowwarfare.gametype.DummyGametype;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;
import it.kytech.bowwarfare.manager.SettingsManager;
import it.kytech.bowwarfare.manager.SettingsManager.OptionFlag;
import it.kytech.bowwarfare.manager.SpawnManager;
import it.kytech.bowwarfare.util.bossbar.BarAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class LastManStanding extends DummyGametype {

    public static String NAME = "LMS";
    public static String LONG_NAME = "Last Man Standing";

    private ArrayList<Location> LMSSpawns;
    private HashMap<Integer, Player> firstFreeSpawn = new HashMap<Integer, Player>();
    private int playerCount = 0;    

    private HashMap<Player, Integer> life = new HashMap<Player, Integer>(); 

    private Scoreboard scoreBoard = sbManager.getNewScoreboard();

    private int count = 20;
    private boolean countdownRunning;
    private int tid = 0;

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

    @Override
    protected void loadDefaultSettings() {
        settings.put(OptionFlag.LMSLIFE, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".life"));
        settings.put(OptionFlag.LMSMINP, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".minp"));
        settings.put(OptionFlag.LMSTIME, SettingsManager.getInstance().getConfig().getInt("limits." + NAME + ".time"));

        saveConfig();
    }    

    @Override
    public boolean onJoin(Player player) {

        if (game.getGameState() != GameState.WAITING) {
            return false;
        }

        player.teleport(LMSSpawns.get(firstFreeSpawn()));

        firstFreeSpawn.put(firstFreeSpawn(), player);
        life.put(player, (Integer) settings.get(SettingsManager.OptionFlag.LMSLIFE));
        playerCount++;

        BarAPI.setMessage(player, buildBossString(LONG_NAME));
        buildScoreBoard(player);

        updateScoreBoard();

        msgmgr.sendFMessage(MessageManager.PrefixType.INFO, "gametype.LMS", player);

        if (playerCount == getMaxPlayer()) {
            game.countdown(10);
        }

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

                BarAPI.removeBar(victim);
                victim.setScoreboard(sbManager.getNewScoreboard());

                game.markAsInactive(victim);
                game.restoreInv(victim);

                msgFall(MessageManager.PrefixType.INFO, "game.playerloosegame", "victim-" + victim.getName(), "killer-" + killer.getName());

                game.addSpectator(victim);

            } else {
                victim.teleport(getRandomSpawnPoint());
            }

            Objective objective = scoreBoard.getObjective(gameID + "." + NAME + "." + "life");
            Score score = objective.getScore(victim.getName());
            score.setScore(lifeCount);
        } else {
            scoreBoard.resetScores(victim.getName());
        }
        return true;
    }

    @Override
    public void checkWin(Player victim, final Player killer) {
        if (game.getActivePlayers() < 2) {

            game.playerWin(victim, killer);

            BarAPI.setMessage(killer, buildBossString(SettingsManager.getInstance().getMessageConfig().getString("messages.game.winner", "You are the Winner! "))); //Blank space to fix visual error!

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Bukkit.getServer().getPluginManager().getPlugin("BowWarfare-Reloaded"), new Runnable() {
                @Override
                public void run() {
                    BarAPI.removeBar(killer);
                }
            }, 10 * 20);
        }
    }

    @Override
    public boolean onPlayerRemove(Player player, boolean hasLeft) {
        for (Object in : firstFreeSpawn.keySet().toArray()) {
            if (firstFreeSpawn.get(in) == player) {
                firstFreeSpawn.remove(in);
            }
        }
        return false;
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
    public boolean onGameStart() {
        for (Player player : game.getAllPlayers()) {
            if (game.isPlayerActive(player)) {
                BarAPI.setMessage(player, buildBossString(LONG_NAME), (Integer) settings.get(OptionFlag.LMSTIME));
            }
        }

        //Run internal countdown
        countdown((Integer) settings.get(OptionFlag.LMSTIME));
        return true;
    }

    /*
     *
     * ################################################
     *
     * COUNTDOWN
     *
     * ################################################
     *
     *
     */
    public int getCountdownTime() {
        return count;
    }

    public void countdown(int time) {
        countdownRunning = true;
        count = time;
        Bukkit.getScheduler().cancelTask(tid);
        if (game.containsTask(tid)) {
            game.removeTask(tid);
        }

        tid = Bukkit.getScheduler().scheduleSyncRepeatingTask((BowWarfare) GameManager.getInstance().getPlugin(), new Runnable() {
            public void run() {
                if (count > 0) {
                    if (count % 60 == 0) {
                        msgFall(PrefixType.INFO, "game.endcountdown", "t-" + count);
                        soundFall(Sound.CLICK);
                    }
                    if (count < 6) {
                        msgFall(PrefixType.INFO, "game.endcountdown", "t-" + count);
                        soundFall(Sound.CLICK);
                    }
                    count--;
                } else {
                    forceWinAPlayer();
                    Bukkit.getScheduler().cancelTask(tid);
                    countdownRunning = false;
                }
            }
        }, 0, 20);

        game.addTask(tid);
    }

    private void forceWinAPlayer() {

        Entry<Player, Integer> best = null;
        Entry<Player, Integer> worst = null;

        for (Entry<Player, Integer> entry : life.entrySet()) {
            if (best == null || entry.getValue() > best.getValue()) {
                best = entry;
            }
            if (worst == null || entry.getValue() < worst.getValue()) {
                worst = entry;
            }
        }

        game.playerWin(worst.getKey(), best.getKey());
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
        Score score = objective.getScore(player.getName());
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

    @Override
    public boolean requireVote() {
        return true;
    }
}