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
package it.kytech.bowwarfare.configuration.holder;

import it.kytech.bowwarfare.reference.DefaultPluginSettings;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;

/**
 * Created by Hitech95 on 24/06/2015.
 */
public class SQLSettings implements IDynHolder {

    private Dbms dbms;
    private int port;
    private String host;
    private String database;
    private String username;
    private String password;
    private String prefix;

    public SQLSettings(Dbms dbms, int port, String host, String database, String username, String password, String prefix) {
        this.dbms = dbms;
        this.port = port;
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.prefix = prefix;
    }


    public SQLSettings(CommentedConfigurationNode sqlNode) {
        this.dbms = Dbms.valueOf(sqlNode.getNode("dbms").getString(DefaultPluginSettings.SQL.DBMS.name()).toUpperCase());
        this.port = sqlNode.getNode("port").getInt(DefaultPluginSettings.SQL.PORT);
        this.host = sqlNode.getNode("host").getString(DefaultPluginSettings.SQL.HOST);
        this.database = sqlNode.getNode("database").getString(DefaultPluginSettings.SQL.DATABASE);
        this.username = sqlNode.getNode("username").getString(DefaultPluginSettings.SQL.USERNAME);
        this.password = sqlNode.getNode("password").getString(DefaultPluginSettings.SQL.PASSWORD);
        this.prefix = sqlNode.getNode("prefix").getString(DefaultPluginSettings.SQL.PREFIX);
    }

    public Dbms getDbms() {
        return dbms;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getPrefix() {
        return prefix;
    }

    @Override
    public void saveData(CommentedConfigurationNode node) {
        node.getNode("port").setValue(port);
        node.getNode("host").setValue(host);
        node.getNode("database").setValue(database);
        node.getNode("username").setValue(username);
        node.getNode("password").setValue(password);
        node.getNode("prefix").setValue(prefix);
    }

    public enum Dbms {
        SQLITE, MYSQL, H2
    }
}
