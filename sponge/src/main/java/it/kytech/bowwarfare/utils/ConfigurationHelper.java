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
package it.kytech.bowwarfare.utils;

import com.google.inject.Inject;
import it.kytech.bowwarfare.reference.Settings;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.service.config.DefaultConfig;

import java.io.File;

/**
 * Created by M2K on 10/04/2015.
 */
public class ConfigurationHelper {

    private static ConfigurationHelper instance;

    @Inject
    @DefaultConfig(sharedRoot = Settings.SHARED_CONFIG)
    private File defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = Settings.SHARED_CONFIG)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    public static ConfigurationHelper getInstance() {
        if (instance == null) {
            instance = new ConfigurationHelper();
        }

        return instance;
    }

    public boolean isDebug() {
        return true; //TODO read config from file
    }
}
