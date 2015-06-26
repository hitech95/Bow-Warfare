/**
 * This file is part of BowWarfare
 * <p>
 * Copyright (c) 2015 hitech95 <https://github.com/hitech95>
 * Copyright (c) contributors
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.kytech.bowwarfare.game;

import it.kytech.bowwarfare.api.game.IArena;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * Created by Hitech95 on 24/06/2015.
 */
public class Arena implements IArena {
    @Override
    public int getID() {
        return 0;
    }

    @Override
    public String getSlug() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String[] getAuthors() {
        return new String[0];
    }

    @Override
    public boolean containsPlayer(Player player) {
        return false;
    }

    @Override
    public boolean isInside(Location location) {
        return false;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public Location getFirstCorner() {
        return null;
    }

    @Override
    public Location getSsecondCorner() {
        return null;
    }
}
