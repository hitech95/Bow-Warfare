package it.kytech.bowwarfare.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.SettingsManager.OptionFlag;

public class Option implements SubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {

        if (!player.hasPermission(permission())) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }

        if (args.length < 3) {
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, help(player), player);
            return true;
        }

        Game g = GameManager.getInstance().getGame(Integer.parseInt(args[0]));

        if (g == null) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.gamedosentexist", player, "arena-" + args[0]);
            return true;
        }

        HashMap<OptionFlag, Object> z = SettingsManager.getInstance().getGameSettings(g.getID());
        z.put(OptionFlag.valueOf(args[1].toUpperCase()), args[2]);
        SettingsManager.getInstance().saveGameSettings(z, g.getID());
        g.reloadConfig();
        
        MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "info.success", player, "command-" + args[0] + " " + args[1] +  args[2]);        
        
        return false;
    }

    @Override
    public String help(Player p) {
        String help = "/bw option <id> <flag> <value> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.flag", "Modifies an arena-specific setting");
        String flags = "";

        for (OptionFlag of : SettingsManager.OptionFlag.values()) {
            flags += of.name().toUpperCase() + ",";
        }

        flags.substring(0, flags.length() - 1);
        return help + "/n" + ChatColor.WHITE + flags;
    }

    @Override
    public String permission() {
        return "bw.admin.option";
    }
}
