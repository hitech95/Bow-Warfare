/**
 * This file is part of BowWarfare
 * <p/>
 * Copyright (c) 2015 hitech95 <https://github.com/hitech95>
 * Copyright (c) contributors
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.kytech.bowwarfare.configuration.holder;

import it.kytech.bowwarfare.reference.DefaultPluginSettings;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

/**
 * Created by Hitech95 on 24/06/2015.
 */
public class EconomySettings implements IDynHolder {

    public int moneyKill;
    public int moneyDeath;
    public int moneyWin;
    public int moneyLose;

    public EconomySettings(int moneyKill, int moneyDeath, int moneyWin, int moneyLose) {
        this.moneyKill = moneyKill;
        this.moneyDeath = moneyDeath;
        this.moneyWin = moneyWin;
        this.moneyLose = moneyLose;
    }

    public EconomySettings(CommentedConfigurationNode ecoNode) {
        this.moneyKill = ecoNode.getNode("money-kill").getInt(DefaultPluginSettings.Economy.MONEY_KILL);
        this.moneyDeath = ecoNode.getNode("money-death").getInt(DefaultPluginSettings.Economy.MONEY_DEATH);
        this.moneyWin = ecoNode.getNode("money-win").getInt(DefaultPluginSettings.Economy.MONEY_WIN);
        this.moneyLose = ecoNode.getNode("money-lose").getInt(DefaultPluginSettings.Economy.MONEY_LOSE);
    }

    @Override
    public void saveData(CommentedConfigurationNode ecoNode) {
        ecoNode.getNode("money-kill").setValue(moneyKill);
        ecoNode.getNode("money-death").setValue(moneyDeath);
        ecoNode.getNode("money-win").setValue(moneyWin);
        ecoNode.getNode("money-lose").setValue(moneyLose);
    }
}
