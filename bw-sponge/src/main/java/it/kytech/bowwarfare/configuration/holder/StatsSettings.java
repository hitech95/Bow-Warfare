/**
 * This file is part of BowWarfare
 *
 * Copyright (c) 2015 hitech95 <https://github.com/hitech95>
 * Copyright (c) contributors
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
package it.kytech.bowwarfare.configuration.holder;

import it.kytech.bowwarfare.reference.DefaultPluginSettings;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

/**
 * Created by Hitech95 on 24/06/2015.
 */
public class StatsSettings {

    public boolean enableSQL;
    public int pointKill;
    public int pointDeath;
    public int pointWin;
    public int pointLose;
    public int pointRankUp;
    public int pointKillstreakMultiplier;
    public int pointKillstreakBase;

    public StatsSettings(boolean enableSQL, int pointKill, int pointDeath, int pointWin, int pointLose, int pointRankUp, int pointKillstreakMultiplier, int pointKillstreakBase) {
        this.enableSQL = enableSQL;
        this.pointKill = pointKill;
        this.pointDeath = pointDeath;
        this.pointWin = pointWin;
        this.pointLose = pointLose;
        this.pointRankUp = pointRankUp;
        this.pointKillstreakMultiplier = pointKillstreakMultiplier;
        this.pointKillstreakBase = pointKillstreakBase;
    }

    public StatsSettings(CommentedConfigurationNode sqlNode) {
        this.enableSQL = sqlNode.getNode("write-sql").getBoolean(DefaultPluginSettings.Stats.WRITE_SQL);
        this.pointKill = sqlNode.getNode("points-kill").getInt(DefaultPluginSettings.Stats.POINT_KILL);
        this.pointDeath = sqlNode.getNode("points-death").getInt(DefaultPluginSettings.Stats.POINT_DEATH);
        this.pointWin = sqlNode.getNode("points-win").getInt(DefaultPluginSettings.Stats.POINT_WIN);
        this.pointLose = sqlNode.getNode("points-lose").getInt(DefaultPluginSettings.Stats.POINT_LOSE);
        this.pointRankUp = sqlNode.getNode("points-rankup").getInt(DefaultPluginSettings.Stats.POINT_RANK_UP);
        this.pointKillstreakMultiplier = sqlNode.getNode("points-killstreak-multiplier").getInt(DefaultPluginSettings.Stats.POINT_KILLSTREAK_MULTIPLIER);
        this.pointKillstreakBase = sqlNode.getNode("points-killstreak-base").getInt(DefaultPluginSettings.Stats.POINT_KILLSTREAK_BASE);
    }

    public boolean isSQLEnabled() {
        return enableSQL;
    }

    public int getPointKill() {
        return pointKill;
    }

    public int getPointDeath() {
        return pointDeath;
    }

    public int getPointWin() {
        return pointWin;
    }

    public int getPointLose() {
        return pointLose;
    }

    public int getPointRankUp() {
        return pointRankUp;
    }

    public int getPointKillstreakMultiplier() {
        return pointKillstreakMultiplier;
    }

    public int getPointKillstreakBase() {
        return pointKillstreakBase;
    }
}
