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
public class GameSettings implements IDynHolder {

    public boolean weather;
    public boolean economy;
    public boolean stats;
    public boolean playerQueue;
    public int rollbackBlocks;
    public int countdownTime;

    public GameSettings(boolean weather, boolean economy, boolean stats, boolean playerQueue, int rollbackBlocks, int countdownTime) {
        this.weather = weather;
        this.economy = economy;
        this.stats = stats;
        this.playerQueue = playerQueue;
        this.rollbackBlocks = rollbackBlocks;
        this.countdownTime = countdownTime;
    }

    public GameSettings(CommentedConfigurationNode gameNode) {
        this.weather = gameNode.getNode("weather").getBoolean(DefaultPluginSettings.Game.WEATHER);
        this.economy = gameNode.getNode("economy").getBoolean(DefaultPluginSettings.Game.ECONOMY);
        this.stats = gameNode.getNode("stats").getBoolean(DefaultPluginSettings.Game.STATS);
        this.playerQueue = gameNode.getNode("player-queue").getBoolean(DefaultPluginSettings.Game.PLAYER_QUEUE);
        this.rollbackBlocks = gameNode.getNode("block-rollback").getInt(DefaultPluginSettings.Game.ROLLBACK);
        this.countdownTime = gameNode.getNode("countdown").getInt(DefaultPluginSettings.Game.COUNTDOWN_TIME);
    }

    public boolean isWeather() {
        return weather;
    }

    public boolean isEconomy() {
        return economy;
    }

    public boolean isStats() {
        return stats;
    }

    public boolean isPlayerQueue() {
        return playerQueue;
    }

    public int getRollbackBlocks() {
        return rollbackBlocks;
    }

    public int getCountdownTime() {
        return countdownTime;
    }

    @Override
    public void saveData(CommentedConfigurationNode gameNode) {
        gameNode.getNode("weather").setValue(weather);
        gameNode.getNode("economy").setValue(economy);
        gameNode.getNode("stats").setValue(stats);
        gameNode.getNode("player-queue").setValue(playerQueue);
        gameNode.getNode("block-rollback").setValue(rollbackBlocks);
        gameNode.getNode("countdown").setValue(countdownTime);
    }
}
