package it.kytech.bowwarfare.manager;

import it.kytech.bowwarfare.BowWarfare;
import it.kytech.bowwarfare.gametype.IGametype;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class SettingsManager {

    private static final int MESSAGE_VERSION = 0;
    private static final int SPAWN_VERSION = 0;
    private static final int SYSTEM_VERSION = 0;
    //makes the config easily accessible
    private static SettingsManager instance = new SettingsManager();
    private static Plugin p;
    private FileConfiguration spawns;
    private FileConfiguration system;
    private FileConfiguration messages;
    private File fileSpawn; //spawns
    private File fileSystem; //system
    private File fileMessages; //messages

    private SettingsManager() {
    }

    public static SettingsManager getInstance() {
        return instance;
    }

    public static World getGameWorld(int game) {
        if (SettingsManager.getInstance().getSystemConfig().getString("bw-system.arenas." + game + ".world") == null) {
            //LobbyManager.getInstance().error(true);
            return null;

        }
        return p.getServer().getWorld(SettingsManager.getInstance().getSystemConfig().getString("bw-system.arenas." + game + ".world"));
    }

    public static String getSqlPrefix() {
        return getInstance().getConfig().getString("sql.prefix");
    }

    public void setup(Plugin p) {
        SettingsManager.p = p;
        if (p.getConfig().getInt("config-version") == BowWarfare.config_version) {
            BowWarfare.config_todate = true;
        } else {
            File config = new File(p.getDataFolder(), "config.yml");
            config.delete();
        }

        p.getConfig().options().copyDefaults(true);
        p.saveDefaultConfig();

        fileSpawn = new File(p.getDataFolder(), "spawns.yml");
        fileSystem = new File(p.getDataFolder(), "system.yml");
        fileMessages = new File(p.getDataFolder(), "messages.yml");

        try {
            if (!fileSpawn.exists()) {
                fileSpawn.createNewFile();
            }
            if (!fileSystem.exists()) {
                fileSystem.createNewFile();
            }
            if (!fileMessages.exists()) {
                loadFile("messages.yml");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        reloadSystem();
        saveSystemConfig();

        reloadSpawns();
        saveSpawns();

        reloadMessages();
        saveMessages();

    }

    public void set(String arg0, Object arg1) {
        p.getConfig().set(arg0, arg1);
    }

    public FileConfiguration getConfig() {
        return p.getConfig();
    }

    public FileConfiguration getSystemConfig() {
        return system;
    }

    public FileConfiguration getSpawns() {
        return spawns;
    }

    public FileConfiguration getMessageConfig() {
        return messages;
    }

    public void saveConfig() {
        // p.saveConfig();
    }

    public void reloadConfig() {
        p.reloadConfig();
    }

    public boolean moveFile(File ff) {
        BowWarfare.$("Moving outdated config file. " + ff.getName());
        String name = ff.getName();
        File ff2 = new File(BowWarfare.getPluginDataFolder(), getNextName(name, 0));
        return ff.renameTo(ff2);
    }

    public String getNextName(String name, int n) {
        File ff = new File(BowWarfare.getPluginDataFolder(), name + ".old" + n);
        if (!ff.exists()) {
            return ff.getName();
        } else {
            return getNextName(name, n + 1);
        }
    }

    public void reloadSpawns() {
        spawns = YamlConfiguration.loadConfiguration(fileSpawn);
        if (spawns.getInt("version", 0) != SPAWN_VERSION) {
            moveFile(fileSpawn);
            reloadSpawns();
        }
        spawns.set("version", SPAWN_VERSION);
        saveSpawns();
    }

    public void reloadSystem() {
        system = YamlConfiguration.loadConfiguration(fileSystem);
        if (system.getInt("version", 0) != SYSTEM_VERSION) {
            moveFile(fileSystem);
            reloadSystem();
        }
        system.set("version", SYSTEM_VERSION);
        saveSystemConfig();
    }

    public void reloadMessages() {
        messages = YamlConfiguration.loadConfiguration(fileMessages);
        if (messages.getInt("version", 0) != MESSAGE_VERSION) {
            moveFile(fileMessages);
            loadFile("messages.yml");
            reloadMessages();
        }
        messages.set("version", MESSAGE_VERSION);
        saveMessages();
    }

    public void saveSystemConfig() {
        try {
            system.save(fileSystem);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveSpawns() {
        try {
            spawns.save(fileSpawn);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveMessages() {
        try {
            messages.save(fileMessages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isSetGameSettings(int gameid, IGametype type) {
        Set<String> values = system.getConfigurationSection("bw-system.arenas." + gameid + ".flags").getKeys(true);

        for (String flag : values.toArray(new String[0])) {
            if (flag.contains(type.getGametypeName().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public HashMap<OptionFlag, Object> getGameSettings(int gameid) {
        HashMap<OptionFlag, Object> flags = new HashMap<OptionFlag, Object>();

        for (OptionFlag f : OptionFlag.values()) {
            if (system.isSet("bw-system.arenas." + gameid + ".flags." + f.toString().toLowerCase())) {
                flags.put(f, system.get("bw-system.arenas." + gameid + ".flags." + f.toString().toLowerCase()));
            }
        }
        return flags;

    }

    public void saveGameSettings(HashMap<OptionFlag, Object> flags, int gameid) {

        for (OptionFlag f : OptionFlag.values()) {
            if (flags.containsKey(f)) {
                system.set("bw-system.arenas." + gameid + ".flags." + f.toString().toLowerCase(), flags.get(f));
            }
        }
        saveSystemConfig();

    }

    public Location getLobbySpawn() {
        try {
            return new Location(Bukkit.getWorld(system.getString("bw-system.lobby.spawn.world")),
                    system.getInt("bw-system.lobby.spawn.x"),
                    system.getInt("bw-system.lobby.spawn.y"),
                    system.getInt("bw-system.lobby.spawn.z"),
                    system.getInt("bw-system.lobby.spawn.yaw"),
                    system.getInt("bw-system.lobby.spawn.pitch"));
        } catch (Exception e) {
            return null;
        }
    }

    public void setLobbySpawn(Location l) {
        system.set("bw-system.lobby.spawn.world", l.getWorld().getName());
        system.set("bw-system.lobby.spawn.x", l.getBlockX());
        system.set("bw-system.lobby.spawn.y", l.getBlockY());
        system.set("bw-system.lobby.spawn.z", l.getBlockZ());
        system.set("bw-system.lobby.spawn.yaw", l.getYaw());
        system.set("bw-system.lobby.spawn.pitch", l.getPitch());

    }

    public void loadFile(String file) {
        File t = new File(p.getDataFolder(), file);
        BowWarfare.$("Writing new file: " + t.getName());

        try {
            t.createNewFile();
            FileWriter out = new FileWriter(t);
            InputStream is = getClass().getResourceAsStream("/" + file);
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null) {
                out.write(line + "\n");
            }
            out.flush();
            is.close();
            isr.close();
            br.close();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static enum OptionFlag {

        MAX_PLAYERS,
        ARENA_NAME,
        GAMETYPE,
        FFAMAXP,
        CTFTIME,
        CTFMAXP,
        CTFMINP,
        TDMMAXP,
        TDMMINP,
        TDMKILL,
        INFTIME,
        INFMAXP,
        INFMINP,
        FFAKILL,
        LMSMINP,
        LMSLIFE,
        LMSTIME
    }
}
