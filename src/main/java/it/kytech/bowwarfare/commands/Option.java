package it.kytech.bowwarfare.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.SettingsManager;

public class Option implements SubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {

        if (!player.hasPermission(permission())) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }

        if (args.length < 2) {
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, help(player), player);
            return true;
        }

        Game g = GameManager.getInstance().getGame(Integer.parseInt(args[0]));

        if (g == null) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.gamedoesntexist", player, "arena-" + args[0]);
            return true;
        }
        try {

            HashMap<SettingsManager.OptionFlag, Object> z = SettingsManager.getInstance().getGameSettings(g.getID());
            z.put(SettingsManager.OptionFlag.valueOf(args[1].toUpperCase()), args[2]);
            SettingsManager.getInstance().saveGameSettings(z, g.getID());
            g.reloadConfig();

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    @Override
    public String help(Player p) {
        return "/bw option <id> <flag> <value> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.flag", "Modifies an arena-specific setting");
    }

    @Override
    public String permission() {
        return "bw.admin.option";
    }
}
