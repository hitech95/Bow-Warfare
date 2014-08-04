package it.kytech.bowwarfare.commands.sub;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.commands.ISubCommand;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.SettingsManager;

public class Disable implements ISubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        try {
            if (args.length == 0) {
                for (Game g : GameManager.getInstance().getGames()) {
                    g.disable();
                }
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "game.all", player, "input-disabled");

            } else {

                GameManager.getInstance().disableGame(Integer.parseInt(args[0]));
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "game.state", player, "arena-" + args[0], "input-disabled");
            }
        } catch (NumberFormatException e) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notanumber", player, "input-Arena");
        } catch (NullPointerException e) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.gamedosentexist", player, "arena-" + args[0]);
        }
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw disable <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.disable", "Disables arena <id>");
    }

    @Override
    public String permission() {
        return "bw.staff.disable";
    }
}
