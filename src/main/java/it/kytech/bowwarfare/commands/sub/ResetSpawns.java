package it.kytech.bowwarfare.commands.sub;

import it.kytech.bowwarfare.commands.ISubCommand;
import it.kytech.bowwarfare.manager.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.SettingsManager;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;

public class ResetSpawns implements ISubCommand {

    public boolean onCommand(Player player, String[] args) {

        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        try {
            if (args.length > 0) {
                GameManager.getInstance().disableGame(Integer.parseInt(args[0]));

                if (args.length > 1) {
                    SettingsManager.getInstance().getSpawns().set("spawns." + Integer.parseInt(args[0]) + "." + args[1].toUpperCase(), null);
                } else {
                    SettingsManager.getInstance().getSpawns().set("spawns." + Integer.parseInt(args[0]), null);
                }
                SettingsManager.getInstance().saveSpawns();
                GameManager.getInstance().enableGame(Integer.parseInt(args[0]));

                MessageManager.getInstance().sendFMessage(PrefixType.INFO, "info.deleted", player, "input-Spawns");
                return false;
            }
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notspecified", player, "input-Game ID");
        } catch (NumberFormatException e) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notanumber", player, "input-Arena");
        } catch (NullPointerException e) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.gamenoexist", player);
        }
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw resetspawns <id> [<GameType>] - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.resetspawns", "Resets spawns for Arena <id>");
    }

    @Override
    public String permission() {
        return "bw.admin.resetspawns";
    }
}
