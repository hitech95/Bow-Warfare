package it.kytech.bowwarfare;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import it.kytech.bowwarfare.MessageManager.PrefixType;
import it.kytech.bowwarfare.commands.AddWall;
import it.kytech.bowwarfare.commands.CreateArena;
import it.kytech.bowwarfare.commands.DelArena;
import it.kytech.bowwarfare.commands.DelWall;
import it.kytech.bowwarfare.commands.Disable;
import it.kytech.bowwarfare.commands.Enable;
import it.kytech.bowwarfare.commands.Option;
import it.kytech.bowwarfare.commands.ForceStart;
import it.kytech.bowwarfare.commands.Join;
import it.kytech.bowwarfare.commands.Leave;
import it.kytech.bowwarfare.commands.LeaveQueue;
import it.kytech.bowwarfare.commands.ListArenas;
import it.kytech.bowwarfare.commands.ListPlayers;
import it.kytech.bowwarfare.commands.Reload;
import it.kytech.bowwarfare.commands.ResetSpawns;
import it.kytech.bowwarfare.commands.SetLobbySpawn;
import it.kytech.bowwarfare.commands.SetSpawn;
import it.kytech.bowwarfare.commands.Spectate;
import it.kytech.bowwarfare.commands.SubCommand;
import it.kytech.bowwarfare.commands.Teleport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.TabCompleter;

public class CommandHandler implements CommandExecutor, TabCompleter {

    private Plugin plugin;
    private HashMap< String, SubCommand> commands;
    private HashMap< String, Integer> helpinfo;
    private MessageManager msgmgr = MessageManager.getInstance();

    public CommandHandler(Plugin plugin) {
        this.plugin = plugin;
        commands = new HashMap< String, SubCommand>();
        helpinfo = new HashMap< String, Integer>();
        loadCommands();
        loadHelpInfo();
    }

    private void loadCommands() {
        commands.put("createarena", new CreateArena());
        commands.put("join", new Join());
        commands.put("addwall", new AddWall());
        commands.put("delwall", new DelWall());
        commands.put("setspawn", new SetSpawn());
        commands.put("listarenas", new ListArenas());
        commands.put("disable", new Disable());
        commands.put("forcestart", new ForceStart());
        commands.put("enable", new Enable());
        commands.put("leave", new Leave());
        commands.put("setlobbyspawn", new SetLobbySpawn());
        commands.put("resetspawns", new ResetSpawns());
        commands.put("delarena", new DelArena());
        commands.put("option", new Option());
        commands.put("spectate", new Spectate());
        commands.put("lq", new LeaveQueue());
        commands.put("leavequeue", new LeaveQueue());
        commands.put("list", new ListPlayers());
        commands.put("tp", new Teleport());
        commands.put("reload", new Reload());
    }

    private void loadHelpInfo() {
        //you can do this by iterating thru the hashmap from a certian index btw instead of using a new hashmap,
        //plus, instead of doing three differnet ifs, just iterate thru and check if the value == the page
        helpinfo.put("createarena", 3);
        helpinfo.put("join", 1);
        helpinfo.put("addwall", 3);
        helpinfo.put("delwall", 3);
        helpinfo.put("setspawn", 3);
        //helpinfo.put("getcount", 3); Don't Exist...
        helpinfo.put("disable", 2);
        helpinfo.put("start", 2);
        helpinfo.put("enable", 2);
        helpinfo.put("leave", 1);
        helpinfo.put("setlobbyspawn", 3);
        helpinfo.put("resetspawns", 3);
        helpinfo.put("delarena", 3);
        helpinfo.put("option", 3);
        helpinfo.put("spectate", 1);
        helpinfo.put("lq", 1);
        helpinfo.put("list", 1);
        helpinfo.put("reload", 3);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd1, String commandLabel, String[] args) {
        PluginDescriptionFile pdfFile = plugin.getDescription();
        if (!(sender instanceof Player)) {
            msgmgr.logMessage(PrefixType.WARNING, "Only in-game players can use BowWafare commands! ");
            return true;
        }

        Player player = (Player) sender;

        if (BowWarfare.config_todate == false) {
            msgmgr.sendMessage(PrefixType.WARNING, "The config file is out of date. Please tell an administrator to reset the config.", player);
            return true;
        }

        if (BowWarfare.dbcon == false) {
            msgmgr.sendMessage(PrefixType.WARNING, "Could not connect to server. Plugin disabled.", player);
            return true;
        }

        if (cmd1.getName().equalsIgnoreCase("bowwarfare")) {
            if (args == null || args.length < 1) {
                msgmgr.sendMessage(PrefixType.INFO, "Version " + pdfFile.getVersion() + " by Hitech95", player);
                msgmgr.sendMessage(PrefixType.INFO, "Type /bw help <player | staff | admin> for command information", player);
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) {
                if (args.length == 1) {
                    help(player, 1);
                } else {
                    if (args[1].toLowerCase().startsWith("player")) {
                        help(player, 1);
                        return true;
                    }
                    if (args[1].toLowerCase().startsWith("staff")) {
                        help(player, 2);
                        return true;
                    }
                    if (args[1].toLowerCase().startsWith("admin")) {
                        help(player, 3);
                        return true;
                    } else {
                        msgmgr.sendMessage(PrefixType.WARNING, args[1] + " is not a valid page! Valid pages are Player, Staff, and Admin.", player);
                    }
                }
                return true;
            }
            String sub = args[0];
            Vector< String> l = new Vector< String>();
            l.addAll(Arrays.asList(args));
            l.remove(0);
            args = (String[]) l.toArray(new String[0]);
            if (!commands.containsKey(sub)) {
                msgmgr.sendMessage(PrefixType.WARNING, "Command doesn't exist.", player);
                msgmgr.sendMessage(PrefixType.INFO, "Type /bw help for command information", player);
                return true;
            }
            try {
                commands.get(sub).onCommand(player, args);
            } catch (Exception e) {
                e.printStackTrace();
                msgmgr.sendFMessage(PrefixType.ERROR, "error.command", player, "command-[" + sub + "] " + Arrays.toString(args));
                msgmgr.sendMessage(PrefixType.INFO, "Type /bw help for command information", player);
            }
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmnd, String commandLabel, String[] args) {
        PluginDescriptionFile pdfFile = plugin.getDescription();
        if (!(sender instanceof Player)) {
            msgmgr.logMessage(PrefixType.WARNING, "Only in-game players can use BowWafare commands! ");
            return null;
        }

        Player player = (Player) sender;

        if (BowWarfare.config_todate == false) {
            msgmgr.sendMessage(PrefixType.WARNING, "The config file is out of date. Please tell an administrator to reset the config.", player);
            return null;
        }

        if (BowWarfare.dbcon == false) {
            msgmgr.sendMessage(PrefixType.WARNING, "Could not connect to server. Plugin disabled.", player);
            return null;
        }

        if (cmnd.getName().equalsIgnoreCase("bowwarfare")) {

            List<String> list = new ArrayList<String>(commands.keySet());
            List<String> retList = new ArrayList<String>();

            Collections.sort(list);

            if (args == null || args.length < 1) {
                return list;
            } else if (args.length > 0) {
                if (args.length == 1) {                    
                    for (String s : list) {
                        if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                            try {
                                retList.add(s);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    Collections.sort(retList);
                    return retList;
                } else {
                    if (commands.containsKey(args[0])) {
                        msgmgr.sendMessage(PrefixType.WARNING, commands.get(args[0]).help(player), player);
                    }
                }
            }
        }

        return new ArrayList<String>();
    }

    public void help(Player p, int page) {
        if (page == 1) {
            p.sendMessage(ChatColor.BLUE + "------------ " + msgmgr.pre + ChatColor.DARK_AQUA + " Player Commands" + ChatColor.BLUE + " ------------");
        }
        if (page == 2) {
            p.sendMessage(ChatColor.BLUE + "------------ " + msgmgr.pre + ChatColor.DARK_AQUA + " Staff Commands" + ChatColor.BLUE + " ------------");
        }
        if (page == 3) {
            p.sendMessage(ChatColor.BLUE + "------------ " + msgmgr.pre + ChatColor.DARK_AQUA + " Admin Commands" + ChatColor.BLUE + " ------------");
        }

        for (String command : commands.keySet()) {
            try {
                if (helpinfo.get(command) == page) {

                    msgmgr.sendMessage(PrefixType.INFO, commands.get(command).help(p), p);
                }
            } catch (Exception e) {
            }
        }

    }
}
