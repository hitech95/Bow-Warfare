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
package it.kytech.bowwarfare.reference;

import it.kytech.bowwarfare.configuration.holder.SQLSettings.Dbms;

/**
 * Created by Hitech95 on 24/06/2015.
 */
public class DefaultPluginSettings {

    public static final int CONFIG_REVISION = 1;

    public static final boolean DEBUG = true;
    public static final boolean UPDATE = true;
    public static final boolean COMMANDS_DISABLE = true;

    public static class Game {
        public static final boolean WEATHER = false;
        public static final boolean ECONOMY = true;
        public static final boolean STATS = true;
        public static final boolean PLAYER_QUEUE = true;
        public static final int ROLLBACK = 100;
        public static final int COUNTDOWN_TIME = 20;
    }

    public static class Stats {
        public static final boolean WRITE_SQL = false;
        public static final int POINT_KILL = 100;
        public static final int POINT_DEATH = -50;
        public static final int POINT_WIN = 500;
        public static final int POINT_LOSE = -150;
        public static final int POINT_RANK_UP = 50;
        public static final int POINT_KILLSTREAK_MULTIPLIER = 10;
        public static final int POINT_KILLSTREAK_BASE = 5;
    }

    public static class Economy {
        public static final int MONEY_KILL = 5;
        public static final int MONEY_DEATH = 1;
        public static final int MONEY_WIN = 10;
        public static final int MONEY_LOSE = 5;
    }

    public static class SQL {
        public static final Dbms DBMS = Dbms.SQLITE;
        public static final int PORT = 3306;
        public static final String HOST = "localhost";
        public static final String DATABASE = "minigame";
        public static final String USERNAME = "bow";
        public static final String PASSWORD = "";
        public static final String PREFIX = "bw_";
    }

}
