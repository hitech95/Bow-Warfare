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
package it.kytech.bowwarfare.configuration;

import it.kytech.bowwarfare.api.game.IArena;
import it.kytech.bowwarfare.configuration.holder.ArenaSettings;
import it.kytech.bowwarfare.reference.Settings;
import it.kytech.bowwarfare.utils.ConfigurationHelper;
import it.kytech.bowwarfare.utils.LogHelper;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by Hitech95 on 24/06/2015.
 */
public class ArenaConfiguration {

    File basePath;
    ConfigurationLoader arenasLoader;
    ConfigurationNode arenasNode;

    public ArenaConfiguration(Path basePathIn) {
        this.basePath = new File(basePathIn.toFile(), Settings.ARENA_FOLDER);
        File arenasConfig = new File(this.basePath, Settings.ARENA_CONFIG);

        try {
            if (!basePath.exists()) {
                basePath.mkdirs();
            }
            if (!arenasConfig.exists()) {
                arenasConfig.createNewFile();
            }

            arenasLoader = ConfigurationHelper.getLoader(arenasConfig.toPath());
            arenasNode = arenasLoader.createEmptyNode(ConfigurationOptions.defaults()).getNode("arenas");

        } catch (IOException e) {
            LogHelper.getInstance().get().getLogger().debug(e.getMessage());
        }
    }

    public String[] listArenas() {
        return new String[0]; //TODO
    }

    public ArenaSettings loadArena(String slug) {
        File arenaConfig = new File(basePath, slug + File.separator + Settings.ARENA_CONFIG);

        try {
            ConfigurationLoader loader = ConfigurationHelper.getLoader(arenaConfig.toPath());
            ConfigurationNode arenaNode = loader.createEmptyNode(ConfigurationOptions.defaults()).getNode("data");
            return new ArenaSettings(arenaNode);
        } catch (IOException e) {
            LogHelper.getInstance().get().getLogger().debug(e.getMessage());
            return null;
        }
    }

    public ArenaSettings loadArena(int id) {
        return null; //TODO
    }

    public boolean storeArena(IArena arena) {
        File arenaConfig = new File(basePath, Settings.ARENA_CONFIG + File.separator + arena.getSlug());
        try {
            ConfigurationLoader loader = ConfigurationHelper.getLoader(arenaConfig.toPath());
            ConfigurationNode arenaNode = loader.createEmptyNode(ConfigurationOptions.defaults()).getNode("data");
            new ArenaSettings(arena);
            loader.save(arenaNode);
        } catch (IOException e) {
            LogHelper.getInstance().get().getLogger().debug(e.getMessage());
            return false;
        }

        return true;
    }
}
