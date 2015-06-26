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
package it.kytech.bowwarfare.configuration.holder;

import com.google.common.base.Functions;
import it.kytech.bowwarfare.api.game.IArena;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;

/**
 * Created by Hitech95 on 24/06/2015.
 */
public class ArenaSettings implements IDynHolder {

    public static final ObjectMapper<ArenaSettings> MAPPER;

    static {
        try {
            MAPPER = ObjectMapper.forClass(ArenaSettings.class);
        } catch (ObjectMappingException e) {
            throw new ExceptionInInitializerError(e);
        }
    }


    int id;
    String slug;
    String world;

    int x1;
    int y1;
    int z1;

    int x2;
    int y2;
    int z2;

    String name;
    String description;
    String[] authors;

    public ArenaSettings(int id, String slug, World world, Location first, Location second, String name, String description, String[] authors) {
        this.id = id;
        this.slug = slug;
        this.world = world.getName();

        this.x1 = first.getBlockX();
        this.y1 = first.getBlockY();
        this.z1 = first.getBlockZ();

        this.x2 = second.getBlockX();
        this.y2 = second.getBlockY();
        this.z2 = second.getBlockZ();

        this.name = name;
        this.description = description;
        this.authors = authors;
    }

    public ArenaSettings(IArena arena) {
        this.id = arena.getID();
        this.slug = arena.getSlug();
        this.world = arena.getWorld().getName();

        this.x1 = arena.getFirstCorner().getBlockX();
        this.y1 = arena.getFirstCorner().getBlockY();
        this.z1 = arena.getFirstCorner().getBlockZ();

        this.x2 = arena.getSsecondCorner().getBlockX();
        this.y2 = arena.getSsecondCorner().getBlockY();
        this.z2 = arena.getSsecondCorner().getBlockZ();

        this.name = arena.getName();
        this.description = arena.getDescription();
        this.authors = arena.getAuthors();
    }

    public ArenaSettings(ConfigurationNode sqlNode) {
        id = sqlNode.getNode("id").getInt();
        slug = sqlNode.getNode("money-kill").getString();
        world = sqlNode.getNode("world").getString();
        x1 = sqlNode.getNode("first-x").getInt();
        y1 = sqlNode.getNode("first-y").getInt();
        z1 = sqlNode.getNode("first-z").getInt();
        x2 = sqlNode.getNode("second-x").getInt();
        y2 = sqlNode.getNode("second-y").getInt();
        z2 = sqlNode.getNode("second-z").getInt();
        name = sqlNode.getNode("name").getString();
        description = sqlNode.getNode("description").getString();
        List<String> authorsList = sqlNode.getNode("authors").getList(Functions.toStringFunction());
        authors = authorsList.toArray(new String[authorsList.size()]);
    }

    @Override
    public void saveData(CommentedConfigurationNode sqlNode) {
        sqlNode.getNode("id").setValue(id);
        sqlNode.getNode("money-kill").setValue(slug);
        sqlNode.getNode("world").setValue(world);
        sqlNode.getNode("first-x").setValue(x1);
        sqlNode.getNode("first-y").setValue(y1);
        sqlNode.getNode("first-z").setValue(z1);
        sqlNode.getNode("second-x").setValue(x2);
        sqlNode.getNode("second-y").setValue(y2);
        sqlNode.getNode("second-z").setValue(z2);
        sqlNode.getNode("name").setValue(name);
        sqlNode.getNode("description").setValue(description);
        sqlNode.getNode("authors").setValue(authors);
    }
}
