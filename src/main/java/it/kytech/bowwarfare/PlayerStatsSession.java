package it.kytech.bowwarfare;

import it.kytech.bowwarfare.manager.SettingsManager;
import it.kytech.bowwarfare.manager.StatsManager;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class PlayerStatsSession implements Comparable<PlayerStatsSession> {

    public Player player;

    public int kills = 0;
    public int death = 0;
    public int arena = 0;

    private boolean winner = false;

    private long lastKill = 0;

    private int ksLevel = 0;
    private HashMap<Integer, Integer> ksList = new HashMap<Integer, Integer>();

    public PlayerStatsSession(Player p, int arenaid) {
        this.player = p;
        this.arena = arenaid;

        ksList.put(1, 0);
        ksList.put(2, 0);
        ksList.put(3, 0);
        ksList.put(4, 0);
        ksList.put(5, 0);
    }

    public int addKill() {
        kills++;
        checkKS();
        lastKill = new Date().getTime();
        return ksLevel;
    }

    public void died() {
        death++;
        ksLevel = -1;
    }

    public void win() {
        winner = true;
    }

    public boolean checkKS() {
        if (15000 > new Date().getTime() - lastKill) {
            ksLevel++;
            addkillStreak(ksLevel);
            return true;
        }
        ksLevel = 0;
        return false;
    }

    public void addkillStreak(int ks) {
        if (ks < 1) {
            return;
        }
        if (ks > 5) {
            ks = 5;
        }
        ksList.put(ks, ksList.get(ks) + 1);
    }

    public int calcPoints() {
        FileConfiguration c = SettingsManager.getInstance().getConfig();
        int killPoints = kills * c.getInt("stats.points.kill");
        int posPoints = StatsManager.getInstance().getPosition(arena, this) * c.getInt("stats.points.position");
        int ksPoints = calcKillStreakPoints();

        return killPoints + posPoints + ksPoints;
    }

    public int calcKillStreakPoints() {
        int points = SettingsManager.getInstance().getConfig().getInt("stats.points.killstreak.base");

        for (int key : ksList.keySet()) {
            points += ksList.get(key) * SettingsManager.getInstance().getConfig().getInt("stats.points.killstreak.multiplier");
        }

        return points;
    }

    public Collection<Integer> getKillStreak() {
        return ksList.values();
    }

    @Override
    public int compareTo(PlayerStatsSession t) {
        if (t == null) {
            throw new NullPointerException();
        }

        if (t.kills < this.kills) {
            return this.kills - t.kills;
        }

        if (t.kills > this.kills) {
            return -(t.kills - this.kills);
        }

        return 0;
    }
}
