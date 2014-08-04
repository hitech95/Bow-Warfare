package it.kytech.bowwarfare.commands.sub;

import it.kytech.bowwarfare.commands.ISubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.SettingsManager;

public class Spectate implements ISubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }

        if (args.length == 0) {
            if (GameManager.getInstance().isSpectator(player)) {
                GameManager.getInstance().removeSpectator(player);
                return true;
            } else {
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notspecified", player, "input-Game ID");
                return true;
            }
        }
        if (GameManager.getInstance().getGame(Integer.parseInt(args[0])).getGameMode() == null) {
            return true;
        }
        if (GameManager.getInstance().getGame(Integer.parseInt(args[0])).getGameMode().getSpawnCount() == 0) {
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.nospawns", player);
            return true;
        }
        if (GameManager.getInstance().isPlayerActive(player)) {
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.specingame", player);
            return true;
        }
        GameManager.getInstance().getGame(Integer.parseInt(args[0])).addSpectator(player);
        return false;
    }

    @Override
    public String help(Player p) {
        return "/bw spectate <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.spectate", "Spectate a running arena");
    }

    @Override
    public String permission() {
        return "bw.user.spectate";
    }

}
