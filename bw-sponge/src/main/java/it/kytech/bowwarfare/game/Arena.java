/**
 * This file is part of BowWarfare
 *
 * Copyright (c) 2016 hitech95 <https://github.com/hitech95>
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
package it.kytech.bowwarfare.game;

import it.kytech.bowwarfare.api.exception.WorldExistException;
import it.kytech.bowwarfare.api.game.IArena;
import it.kytech.bowwarfare.configuration.holder.ArenaSettings;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * Created by Hitech95 on 24/06/2015.
 */
public class Arena implements IArena {

    private int id;

    private String slug;
    private String name;
    private String description;
    private String[] authors;

    private boolean enabled;

    private World world;

    private Location first;
    private Location last;

    public Arena(Server server, ArenaSettings settings) throws WorldExistException {
        this.id = settings.getId();
        slug = settings.getSlug();
        name = settings.getName();
        description = settings.getDescription();
        authors = settings.getAuthors();
        enabled = false; //TODO

        Optional<World> worldO = server.getWorld(settings.getWorld());
        if (worldO.isPresent()) {
            world = worldO.get();
        } else {
            throw new WorldExistException();
        }

        first = new Location(world, settings.getX1(), settings.getY1(), settings.getZ1());
        last = new Location(world, settings.getX2(), settings.getY2(), settings.getZ2());
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String getSlug() {
        return slug;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String[] getAuthors() {
        return authors;
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
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Location getFirstCorner() {
        return first;
    }

    @Override
    public Location getSsecondCorner() {
        return last;
    }
}
