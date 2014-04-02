package it.kytech.bowwarfare;

import it.kytech.bowwarfare.gametype.Gametype;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class SettingsManager {

    //makes the config easily accessible
    private static SettingsManager instance = new SettingsManager();
    private static Plugin p;
    private FileConfiguration spawns;
    private FileConfiguration system;
    private FileConfiguration kits;
    private FileConfiguration messages;
    private File f; //spawns
    private File f2; //system
    private File f3; //kits
    private File f4; //messages
    private static final int KIT_VERSION = 1;
    private static final int MESSAGE_VERSION = 1;
    private static final int SPAWN_VERSION = 0;
    private static final int SYSTEM_VERSION = 0;

    public static enum OptionFlag {

        MAX_PLAYERS, ARENA_NAME, GAMETYPE, FFAMAXP, CTFTIME, CTFMAXP, CTFMINP, TDMTIME, TDMMAXP, TDMMINP, INFTIME, INFMAXP, INFMINP, FFAKILL, TDMKILL, LMSMAXP
    }

    private SettingsManager() {
    }

    public static SettingsManager getInstance() {
        return instance;
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

        f = new File(p.getDataFolder(), "spawns.yml");
        f2 = new File(p.getDataFolder(), "system.yml");
        f3 = new File(p.getDataFolder(), "kits.yml");
        f4 = new File(p.getDataFolder(), "messages.yml");

        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            if (!f2.exists()) {
                f2.createNewFile();
            }
            if (!f3.exists()) {
                loadFile("kits.yml");
            }
            if (!f4.exists()) {
                loadFile("messages.yml");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        reloadSystem();
        saveSystemConfig();

        reloadSpawns();
        saveSpawns();

        reloadKits();
        //saveKits();

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

    public FileConfiguration getKits() {
        return kits;
    }

    public FileConfiguration getMessageConfig() {
        return messages;
    }

    public void saveConfig() {
        // p.saveConfig();
    }

    public static World getGameWorld(int game) {
        if (SettingsManager.getInstance().getSystemConfig().getString("bw-system.arenas." + game + ".world") == null) {
            //LobbyManager.getInstance().error(true);
            return null;

        }
        return p.getServer().getWorld(SettingsManager.getInstance().getSystemConfig().getString("bw-system.arenas." + game + ".world"));
    }

    public void reloadConfig() {
        p.reloadConfig();
    }

    public boolean moveFile(File ff) {
        BowWarfare.$("Moving outdated config file. " + f.getName());
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
        spawns = YamlConfiguration.loadConfiguration(f);
        if (spawns.getInt("version", 0) != SPAWN_VERSION) {
            moveFile(f);
            reloadSpawns();
        }
        spawns.set("version", SPAWN_VERSION);
        saveSpawns();
    }

    public void reloadSystem() {
        system = YamlConfiguration.loadConfiguration(f2);
        if (system.getInt("version", 0) != SYSTEM_VERSION) {
            moveFile(f2);
            reloadSystem();
        }
        system.set("version", SYSTEM_VERSION);
        saveSystemConfig();
    }

    public void reloadKits() {
        kits = YamlConfiguration.loadConfiguration(f3);
        if (kits.getInt("version", 0) != KIT_VERSION) {
            moveFile(f3);
            loadFile("kits.yml");
            reloadKits();
        }

    }

    public void reloadMessages() {
        messages = YamlConfiguration.loadConfiguration(f4);
        if (messages.getInt("version", 0) != MESSAGE_VERSION) {
            moveFile(f4);
            loadFile("messages.yml");
            reloadKits();
        }
        messages.set("version", MESSAGE_VERSION);
        saveMessages();
    }

    public void saveSystemConfig() {
        try {
            system.save(f2);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveSpawns() {
        try {
            spawns.save(f);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveKits() {
        try {
            kits.save(f3);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void saveMessages() {
        try {
            messages.save(f4);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int getMaxPlayerCount(int gameid, Gametype type) {
        return system.getInt("bw-system.arenas." + gameid + ".flags.+" + type.getGametypeName().toLowerCase() + "-maxp");
    }

    public boolean isSetGameSettings(int gameid, Gametype type) {
        return system.isSet("bw-system.arenas." + gameid + ".flags." + type.getGametypeName().toLowerCase() + "-maxp");
    }

    //TODO: Implement per-arena settings aka flags
    public HashMap<OptionFlag, Object> getGameSettings(int gameid) {
        HashMap<OptionFlag, Object> flags = new HashMap<OptionFlag, Object>();

        for (OptionFlag f : OptionFlag.values()) {
            if (system.isSet("bw-system.arenas." + gameid + ".flags." + f.toString().toLowerCase())) {
                flags.put(f, system.getString("bw-system.arenas." + gameid + ".flags." + f.toString().toLowerCase()));
            }
        }

        /*//General
         flags.put(OptionFlag.ARENA_NAME, system.getString("bw-system.arenas." + gameid + ".flags.arenaname"));
         flags.put(OptionFlag.GAMETYPE, system.getString("bw-system.arenas." + gameid + ".flags.gametype"));

         //FFA
         flags.put(OptionFlag.FFAMAXP, system.getInt("bw-system.arenas." + gameid + ".flags.ffa-maxp"));
         flags.put(OptionFlag.FFAKILL, system.getInt("bw-system.arenas." + gameid + ".flags.ffa-kill"));

         //CTF
         flags.put(OptionFlag.CTFTIME, system.getInt("bw-system.arenas." + gameid + ".flags.ctf-time"));
         flags.put(OptionFlag.CTFMAXP, system.getInt("bw-system.arenas." + gameid + ".flags.cft-maxp"));
         flags.put(OptionFlag.CTFMINP, system.getInt("bw-system.arenas." + gameid + ".flags.cft-minp"));

         //TDM
         flags.put(OptionFlag.TDMTIME, system.getInt("bw-system.arenas." + gameid + ".flags.tdm-time"));
         flags.put(OptionFlag.TDMMAXP, system.getInt("bw-system.arenas." + gameid + ".flags.tdm-maxp"));
         flags.put(OptionFlag.TDMMINP, system.getInt("bw-system.arenas." + gameid + ".flags.tdm-minp"));
         flags.put(OptionFlag.TDMKILL, system.getInt("bw-system.arenas." + gameid + ".flags.tdm-kill"));

         //INF
         flags.put(OptionFlag.INFTIME, system.getInt("bw-system.arenas." + gameid + ".flags.inf-time"));
         flags.put(OptionFlag.INFMAXP, system.getInt("bw-system.arenas." + gameid + ".flags.inf-maxp"));
         flags.put(OptionFlag.INFMINP, system.getInt("bw-system.arenas." + gameid + ".flags.inf-minp"));
         */
        return flags;

    }

    public void saveGameSettings(HashMap<OptionFlag, Object> flags, int gameid) {

        for (OptionFlag f : OptionFlag.values()) {
            if (flags.containsKey(f)) {
                system.set("bw-system.arenas." + gameid + ".flags." + f.toString().toLowerCase(), flags.get(f));
            }
        }

        /*system.set("bw-system.arenas." + gameid + ".flags.arenaname", flags.get(OptionFlag.ARENA_NAME));
         system.set("bw-system.arenas." + gameid + ".flags.gametype", flags.get(OptionFlag.GAMETYPE));

         //FFA
         system.set("bw-system.arenas." + gameid + ".flags.ffa-maxp", flags.get(OptionFlag.FFAMAXP));
         system.set("bw-system.arenas." + gameid + ".flags.ffa-kill", flags.get(OptionFlag.FFAKILL));

         //CTF
         system.set("bw-system.arenas." + gameid + ".flags.ctf-time", flags.get(OptionFlag.CTFTIME));
         system.set("bw-system.arenas." + gameid + ".flags.cft-maxp", flags.get(OptionFlag.CTFMAXP));
         system.set("bw-system.arenas." + gameid + ".flags.cft-minp", flags.get(OptionFlag.CTFMINP));

         //TDM
         system.set("bw-system.arenas." + gameid + ".flags.tdm-time", flags.get(OptionFlag.TDMTIME));
         system.set("bw-system.arenas." + gameid + ".flags.tdm-maxp", flags.get(OptionFlag.TDMMAXP));
         system.set("bw-system.arenas." + gameid + ".flags.tdm-minp", flags.get(OptionFlag.TDMMINP));
         system.set("bw-system.arenas." + gameid + ".flags.tdm-kill", flags.get(OptionFlag.TDMKILL));

         //INF
         system.set("bw-system.arenas." + gameid + ".flags.inf-time", flags.get(OptionFlag.INFTIME));
         system.set("bw-system.arenas." + gameid + ".flags.inf-maxp", flags.get(OptionFlag.INFMAXP));
         system.set("bw-system.arenas." + gameid + ".flags.inf-minp", flags.get(OptionFlag.INFMINP));*/
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

    public static String getSqlPrefix() {
        return getInstance().getConfig().getString("sql.prefix");
    }

    public void loadFile(String file) {
        File t = new File(p.getDataFolder(), file);
        System.out.println("Writing new file: " + t.getName());

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
}
