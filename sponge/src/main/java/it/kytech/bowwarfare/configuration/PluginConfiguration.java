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
package it.kytech.bowwarfare.configuration;

import it.kytech.bowwarfare.configuration.holder.EconomySettings;
import it.kytech.bowwarfare.configuration.holder.GameSettings;
import it.kytech.bowwarfare.configuration.holder.SQLSettings;
import it.kytech.bowwarfare.reference.DefaultPluginSettings;
import it.kytech.bowwarfare.utils.ConfigurationHelper;
import it.kytech.bowwarfare.utils.LogHelper;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;

/**
 * Created by Hitech95 on 24/06/2015.
 */
public class PluginConfiguration {

    LogHelper logger;
    ConfigurationLoader<CommentedConfigurationNode> loader;
    CommentedConfigurationNode configNode;

    public PluginConfiguration(File config) {
        logger = LogHelper.getInstance().get();
        int configRev = 0;
        try {
            loader = ConfigurationHelper.getLoader(config);
            configNode = loader.load();
            configRev = configNode.getNode("revision").getInt(0);
        } catch (IOException e) {
            logger.debug(e.getMessage());
            logger.logWarn("Resetting config...");
            configNode = loader.createEmptyNode(ConfigurationOptions.defaults());
            configRev = 0;
        }

        if (configRev != DefaultPluginSettings.CONFIG_REVISION) {
            logger.logWarn("Loading default config. Version detected: " + configRev);
            loadDefaultConfigValues();
            saveData();
        }
    }

    private void loadDefaultConfigValues() {
        configNode.getNode("revision").setValue(DefaultPluginSettings.CONFIG_REVISION);
        configNode.getNode("debug").setValue(DefaultPluginSettings.DEBUG);
        configNode.getNode("update").setValue(DefaultPluginSettings.UPDATE);
        sqlSettings().saveData(configNode.getNode("sql"));
        gameSettings().saveData(configNode.getNode("game"));
        economySettings().saveData(configNode.getNode("economy"));
    }

    public void saveData() {
        try {
            loader.save(configNode);
        } catch (IOException e) {
            logger.debug(e.getMessage());
        }
    }

    public boolean isDebug() {
        return configNode.getNode("debug").getBoolean(DefaultPluginSettings.DEBUG);
    }

    public boolean checkUpdates() {
        return configNode.getNode("update").getBoolean(DefaultPluginSettings.UPDATE);
    }

    public SQLSettings sqlSettings() {
        return new SQLSettings(configNode.getNode("sql"));
    }

    public GameSettings gameSettings() {
        return new GameSettings(configNode.getNode("game"));
    }

    public EconomySettings economySettings() {
        return new EconomySettings(configNode.getNode("economy"));
    }
}
