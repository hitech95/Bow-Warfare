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

/**
 * Created by Hitech95 on 25/06/2015.
 */
public class Permission {

    public static final String COMMAND = "bw.command";

    public static class User {
        public static final String JOIN_LOBBY = "bw.user.join"; //
        public static final String JOIN_ARENA = "bw.user.join.#"; //Replace '#' with the arena slug &/or ID
        public static final String SPECTATE = "bw.user.spectate";
    }

    public static class Staff {
        public static final String START_GAME = "bw.staff.start";
        public static final String DISABLE_ARENA = "bw.staff.disable_arena";
        public static final String ENABLE_ARENA = "bw.staff.enable_arena";
        public static final String BYPASS_TELEPORT = "bw.staff.bypass_teleport";
        public static final String BYPASS_COMMAND = "bw.staff.bypass_command";
    }

    public static class Admin {
        public static final String CREATE_ARENA = "bw.staff.create_arena";
        public static final String DELETE_ARENA = "bw.staff.delete_arena";
        public static final String SET_ARENA_SPAWNS = "bw.staff.add_arena_spawns";
        public static final String RESET_ARENA_SPAWNS = "bw.staff.reset_spawns";
        public static final String SET_LOBBY_SPAWN = "bw.staff.set_lobby";
        public static final String ADD_LOBBY_WALL = "bw.staff.add_wall";
        public static final String RELOAD_SETTINGS = "bw.staff.reload_settings";
    }
}
