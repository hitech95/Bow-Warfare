package it.kytech.bowwarfare.commands.sub;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.commands.ISubCommand;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;
import it.kytech.bowwarfare.logging.QueueManager;
import it.kytech.bowwarfare.manager.SettingsManager;

public class Reload implements ISubCommand {

    @Override
    public boolean onCommand(final Player player, String[] args) {
        if (player.hasPermission(permission())) {
            if (args.length != 1) {
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "Valid reload types <Settings | Games |All>", player);
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "Settings will reload the settings configs and attempt to reapply them", player);
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "Games will reload all games currently running", player);
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "All will attempt to reload the entire plugin", player);

                return true;

            }
            if (args[0].equalsIgnoreCase("settings")) {
                SettingsManager.getInstance().reloadMessages();
                SettingsManager.getInstance().reloadSpawns();
                SettingsManager.getInstance().reloadSystem();
                SettingsManager.getInstance().reloadConfig();
                for (Game g : GameManager.getInstance().getGames()) {
                    g.reloadConfig();
                }
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "Settings Reloaded", player);
            } else if (args[0].equalsIgnoreCase("games")) {
                for (Game g : GameManager.getInstance().getGames()) {
                    QueueManager.getInstance().rollback(g.getID(), true);
                    g.disable();
                    g.enable();
                }
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "Games Reloaded", player);
            } else if (args[0].equalsIgnoreCase("all")) {
                final Plugin pinstance = GameManager.getInstance().getPlugin();
                Bukkit.getPluginManager().disablePlugin(pinstance);
                Bukkit.getPluginManager().enablePlugin(pinstance);
                MessageManager.getInstance().sendMessage(PrefixType.INFO, "Plugin reloaded", player);
            }

        } else {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.nopermission", player);
        }
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw reload - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.reload", "Reload the configuration");
    }

    @Override
    public String permission() {
        return "bw.admin.reload";
    }

}
