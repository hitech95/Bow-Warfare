package it.kytech.bowwarfare.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import sun.rmi.runtime.Log;

public class DatabaseManager {

    private static DatabaseManager instance = new DatabaseManager();
    private Connection conn;
    private Logger log;

    private DatabaseManager() {

    }

    public static DatabaseManager getInstance() {
        return instance;
    }

    public void setup(Plugin p) {
        log = p.getLogger();
        connect();
    }

    public Connection getMysqlConnection() {
        return conn;
    }

    public boolean connectToDB(String host, int port, String db, String user, String pass) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + db, user, pass);
            return true;
        } catch (ClassNotFoundException e) {
            log.warning("Couldn't start MySQL Driver. Stopping...\n" + e.getMessage());
            return false;
        } catch (SQLException e) {
            log.warning("Couldn't connect to MySQL database. Stopping...\n" + e.getMessage());
            return false;
        }
    }

    public PreparedStatement createStatement(String query) {
        int times = 0;

        PreparedStatement p = null;

        while (times < 5) {
            try {
                times++;
                p = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            } catch (SQLException e) {
                if (times == 5) {
                    log.log(Level.SEVERE, "[BowWarfare][SQL ERROR] ATTEMPTED TO CONNECT TO DATABASE 5 TIMES AND FAILED! CONNECTION LOST.");
                    return null;
                }
                connect();
                continue;
            }
            return p;
        }
        return null;
    }

    public Statement createStatement() {
        try {
            return conn.createStatement();
        } catch (SQLException e) {
            return null;
        }
    }

    public boolean connect() {
        log.log(Level.INFO, "[BowWarfare] Connecting to Database.");
        FileConfiguration c = SettingsManager.getInstance().getConfig();
        String host = c.getString("sql.host", "localhost");
        int port = c.getInt("sql.port", 3306);
        String db = c.getString("sql.database", "BowWafrare");
        String user = c.getString("sql.user", "root");
        String pass = c.getString("sql.pass", "");
        return this.connectToDB(host, port, db, user, pass);
    }

}
