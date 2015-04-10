package it.kytech.bowwarfare.manager;

import it.kytech.bowwarfare.PlayerStatsSession;
import com.google.common.collect.Lists;
import it.kytech.bowwarfare.BowWarfare;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class StatsManager {

    private static StatsManager instance = new StatsManager();
    MessageManager msgmgr;
    private ArrayList<PreparedStatement> queue = new ArrayList<PreparedStatement>();
    private DatabaseDumper dumper = new DatabaseDumper();
    private DatabaseManager dbman = DatabaseManager.getInstance();
    private HashMap<Integer, HashMap<Player, PlayerStatsSession>> arenas = new HashMap<Integer, HashMap<Player, PlayerStatsSession>>();
    private boolean enabled = true;

    private StatsManager() {
        msgmgr = MessageManager.getInstance();;
    }

    public static StatsManager getInstance() {
        return instance;
    }

    public void setup(Plugin p, boolean b) {
        enabled = b;

        if (!enabled) {
            return;
        }

        try {

            DatabaseMetaData dbm = dbman.getMysqlConnection().getMetaData();

            PreparedStatement round = dbman.createStatement(
                    " CREATE TABLE `" + SettingsManager.getSqlPrefix() + "roundstats` ("
                    + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                    + "`arena` int(11) NOT NULL,"
                    + "`winner` varchar(36) NOT NULL,"
                    + "`time` date NOT NULL,"
                    + "PRIMARY KEY (`id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;"
            );

            PreparedStatement player = dbman.createStatement(
                    " CREATE TABLE `" + SettingsManager.getSqlPrefix() + "playerstats` ("
                    + "`id` int(11) NOT NULL AUTO_INCREMENT,"
                    + "`round` int(11) NOT NULL,"
                    + "`player` varchar(36) NOT NULL,"
                    + "`arenaId` int(11) NOT NULL,"
                    + "`point` int(11) NOT NULL,"
                    + "`kills` int(11) NOT NULL,"
                    + "`death` int(11) NOT NULL,"
                    + "`k1` int(11) NOT NULL,"
                    + "`k2` int(11) NOT NULL,"
                    + "`k3` int(11) NOT NULL,"
                    + "`k4` int(11) NOT NULL,"
                    + "`k5` int(11) NOT NULL,"
                    + "PRIMARY KEY (`id`),"
                    + "KEY `round` (`round`),"
                    + "FOREIGN KEY (`round`) REFERENCES `" + SettingsManager.getSqlPrefix() + "roundstats` (`id`)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1 ;"
            );

            ResultSet roundTable = dbm.getTables(null, null, SettingsManager.getSqlPrefix() + "roundstats", null);
            ResultSet playerTable = dbm.getTables(null, null, SettingsManager.getSqlPrefix() + "playerstats", null);

            if (!roundTable.next()) {
                round.execute();
            }
            if (!playerTable.next()) {
                player.execute();
            }
        } catch (Exception e) {
            enabled = false;
            BowWarfare.$(Level.SEVERE, e.getLocalizedMessage());
        }
    }

    public void addArena(int arenaid) {
        arenas.put(arenaid, new HashMap<Player, PlayerStatsSession>());
    }

    public void addPlayer(Player p, int arenaid) {
        arenas.get(arenaid).put(p, new PlayerStatsSession(p, arenaid));
    }

    public void removePlayer(Player p, int id) {
        arenas.get(id).remove(p);
    }

    public void playerDied(Player p, int arenaid) {
        arenas.get(arenaid).get(p).died();
    }

    public void playerWin(Player p, int arenaid) {
        arenas.get(arenaid).get(p).win();
    }

    public void addKill(Player p, int arenaid) {
        PlayerStatsSession s = arenas.get(arenaid).get(p);

        int kslevel = s.addKill();

        if (kslevel > 3) {
            msgmgr.broadcastFMessage(PrefixType.INFO, "killstreak.level" + ((kslevel > 5) ? 5 : kslevel), "player-" + p.getName());
        } else if (kslevel > 0) {
            for (Player pl : GameManager.getInstance().getGame(arenaid).getAllPlayers()) {
                msgmgr.sendFMessage(PrefixType.INFO, "killstreak.level" + ((kslevel > 5) ? 5 : kslevel), pl, "player-" + p.getName());
            }
        }
    }

    public int getPosition(int arena, PlayerStatsSession player) {
        ArrayList<PlayerStatsSession> list = new ArrayList<PlayerStatsSession>(arenas.get(arena).values());
        Collections.sort(list);
        List<PlayerStatsSession> reverse = Lists.reverse(list);

        return 1 + Collections.binarySearch(reverse, player);
    }

    public void saveGame(int arenaid, Player winner, long time) {
        FileConfiguration config = SettingsManager.getInstance().getConfig();
        int lastID = -1;

        if (enabled) {
            try {
                PreparedStatement roundQuery = dbman.createStatement("INSERT INTO " + SettingsManager.getSqlPrefix() + "roundstats VALUES(NULL," + arenaid + ",'" + winner.getUniqueId() + "'," + time + ")");
                lastID = roundQuery.executeUpdate();
            } catch (SQLException e) {
                BowWarfare.$(Level.SEVERE, "Error while updating DB stats");
                BowWarfare.$(Level.SEVERE, e.getLocalizedMessage());
                return;
            }
        }

        for (PlayerStatsSession s : arenas.get(arenaid).values()) {

            EconomyManager.getInstance().executeTask(s.calcPoints() * config.getDouble("stats.points.price"), s.player);
            MessageManager.getInstance().sendMessage(PrefixType.INFO, "You have earned " + s.calcPoints() * config.getDouble("stats.points.price") + "$ participating in this round!", s.player);

            if (enabled) {
                ArrayList<Integer> killStreak = new ArrayList(s.getKillStreak());

                addSQL("INSERT INTO " + SettingsManager.getSqlPrefix() + "playerstats VALUES("
                        + "NULL,"
                        + lastID + ","
                        + "'" + s.player.getUniqueId() + "',"
                        + arenaid + ","
                        + s.calcPoints() + ","
                        + s.kills + ","
                        + s.death + ","
                        + killStreak.get(0) + ","
                        + killStreak.get(1) + ","
                        + killStreak.get(2) + ","
                        + killStreak.get(3) + ","
                        + killStreak.get(4)
                        + ")");
            }
        }
        arenas.get(arenaid).clear();

    }

    private void addSQL(String query) {
        addSQL(dbman.createStatement(query));
    }

    private void addSQL(PreparedStatement s) {
        queue.add(s);
        if (!dumper.isAlive()) {
            dumper = new DatabaseDumper();
            dumper.start();
        }
    }

    class DatabaseDumper extends Thread {

        public void run() {
            while (queue.size() > 0) {
                PreparedStatement s = queue.remove(0);
                try {
                    s.execute();
                } catch (Exception e) {
                    dbman.connect();
                }

            }
        }
    }
}
