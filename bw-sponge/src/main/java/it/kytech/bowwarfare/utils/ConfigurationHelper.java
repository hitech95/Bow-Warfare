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
package it.kytech.bowwarfare.utils;

import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

/**
 * Created by M2K on 10/04/2015.
 */
public class ConfigurationHelper {

    public static ConfigurationLoader getLoader(Path config) throws IOException {
        if (!config.toFile().exists()) {
            config.toFile().getParentFile().mkdirs();
            config.toFile().createNewFile();
        }
        return HoconConfigurationLoader.builder().setPath(config).build();
    }

    public static ConfigurationLoader getLoader(URL config) throws IOException {
        return HoconConfigurationLoader.builder().setURL(config).build();
    }
}
