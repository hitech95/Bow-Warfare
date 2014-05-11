package it.kytech.bowwarfare;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import it.kytech.bowwarfare.events.*;
import it.kytech.bowwarfare.hooks.HookManager;
import it.kytech.bowwarfare.logging.LoggingManager;
import it.kytech.bowwarfare.logging.QueueManager;
import it.kytech.bowwarfare.stats.StatsManager;
import it.kytech.bowwarfare.util.DatabaseManager;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import it.kytech.bowwarfare.Metrics.Graph;
import it.kytech.bowwarfare.util.EconomyManager;

public class BowWarfare extends JavaPlugin {

    private static File datafolder;
    private static boolean disabling = false;

    public static Logger logger;
    public static boolean dbcon = false;
    public static boolean config_todate = false;
    public static int config_version = 2;

    public static List< String> auth = Arrays.asList(new String[]{
        "Double0negative", "hitech95", "YoshiGenius" //:) -Bryce
    });

    BowWarfare p = this;

    @Override
    public void onDisable() {
        disabling = false;
        PluginDescriptionFile pdfFile = p.getDescription();
        SettingsManager.getInstance().saveSpawns();
        SettingsManager.getInstance().saveSystemConfig();
        for (Game g : GameManager.getInstance().getGames()) {
            try {
                g.disable();
            } catch (Exception e) {
                //will throw useless "tried to register task blah blah error." Use the method below to reset the arena without a task.
            }
            QueueManager.getInstance().rollback(g.getID(), true);
        }

        logger.info(pdfFile.getName() + " version " + pdfFile.getVersion() + " has now been disabled and reset");
    }

    @Override
    public void onEnable() {
        logger = p.getLogger();

        //ensure that all worlds are loaded. Fixes some issues with Multiverse loading after this plugin had started
        getServer().getScheduler().scheduleSyncDelayedTask(this, new Startup(), 10);

        try {

            Metrics metrics = new Metrics(this);

            Graph weaponsUsedGraph = metrics.createGraph("Extra Info");

            weaponsUsedGraph.addPlotter(new Metrics.Plotter("Arena Number") {

                @Override
                public int getValue() {
                    return GameManager.getInstance().getGameCount();
                }

            });

            metrics.start();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    class Startup implements Runnable {

        @Override
        public void run() {
            datafolder = p.getDataFolder();

            SettingsManager.getInstance().setup(p);
            MessageManager.getInstance().setup();

            FileConfiguration c = SettingsManager.getInstance().getConfig();

            if (c.getBoolean("economy.enabled")) {
                EconomyManager.getInstance().setup();
            }

            HookManager.getInstance().setup();

            try { // try loading everything that uses SQL. 
                if (c.getBoolean("stats.enabled")) {
                    DatabaseManager.getInstance().setup(p);
                }
                QueueManager.getInstance().setup(p);
                StatsManager.getInstance().setup(p, c.getBoolean("stats.enabled"));
                dbcon = true;
            } catch (Exception e) {
                dbcon = false;
                e.printStackTrace();
                logger.severe("!!!Failed to connect to the database. Please check the settings and try again!!!");
                return;
            }

            GameManager.getInstance().setup(p);
            LobbyManager.getInstance().setup(p);

            setCommands();

            PluginManager pm = getServer().getPluginManager();

            pm.registerEvents(new PlaceEvent(), p);
            pm.registerEvents(new BreakEvent(), p);
            pm.registerEvents(new DeathEvent(), p);
            pm.registerEvents(new MoveEvent(), p);
            pm.registerEvents(new CommandCatch(), p);
            pm.registerEvents(new SignClickEvent(), p);
            pm.registerEvents(new LogoutEvent(), p);
            pm.registerEvents(new JoinEvent(p), p);
            pm.registerEvents(new TeleportEvent(), p);
            pm.registerEvents(LoggingManager.getInstance(), p);
            pm.registerEvents(new SpectatorEvents(), p);
            pm.registerEvents(new KitEvents(), p);
            pm.registerEvents(new KeepLobbyLoadedEvent(), p);
            pm.registerEvents(new DropItemEvent(), p);
            pm.registerEvents(new EntityShootEvent(), p);
            pm.registerEvents(new FoodLevelEvent(), p);
            pm.registerEvents(new PickupItemEvent(), p);
            pm.registerEvents(new ProjectileEvent(), p);
            pm.registerEvents(new WeatherEvent(), p);

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (GameManager.getInstance().getBlockGameId(p.getLocation()) != -1) {
                    p.teleport(SettingsManager.getInstance().getLobbySpawn());
                }
            }
        }
    }

    public void setCommands() {
        getCommand("bowwarfare").setExecutor(new CommandHandler(p));
    }

    public static File getPluginDataFolder() {
        return datafolder;
    }

    public BowWarfare getPlugin() {
        return p;
    }

    public static boolean isDisabling() {
        return disabling;
    }

    public WorldEditPlugin getWorldEdit() {
        Plugin worldEdit = getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit instanceof WorldEditPlugin) {
            return (WorldEditPlugin) worldEdit;
        } else {
            return null;
        }
    }

    public static void $(String msg) {
        logger.log(Level.INFO, msg);
    }

    public static void $(Level l, String msg) {
        logger.log(l, msg);
    }

    public static void debug(String msg) {
        if (SettingsManager.getInstance().getConfig().getBoolean("debug", false)) {
            $("[Debug] " + msg);
        }
    }

    public static void debug(int a) {
        if (SettingsManager.getInstance().getConfig().getBoolean("debug", false)) {
            debug(a + "");
        }
    }
}
