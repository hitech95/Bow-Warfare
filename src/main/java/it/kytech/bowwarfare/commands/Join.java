package it.kytech.bowwarfare.commands;

import org.bukkit.entity.Player;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.MessageManager.PrefixType;

public class Join implements SubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (args.length == 1) {
            if (player.hasPermission(permission())) {
                try {
                    int a = Integer.parseInt(args[0]);
                    GameManager.getInstance().addPlayer(player, a);
                } catch (NumberFormatException e) {
                    MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notanumber", player, "input-" + args[0]);
                }
            } else {
                MessageManager.getInstance().sendFMessage(PrefixType.WARNING, "error.nopermission", player);
            }
        } else {
            if (player.hasPermission(permission())) {
                if (GameManager.getInstance().getPlayerGameId(player) != -1) {
                    MessageManager.getInstance().sendMessage(PrefixType.ERROR, "error.alreadyingame", player);
                    return true;
                }
                if (SettingsManager.getInstance().getLobbySpawn() == null) {
                    MessageManager.getInstance().sendFMessage(PrefixType.WARNING, "error.nolobbyspawn", player);
                    return false;
                } else {
                    player.teleport(SettingsManager.getInstance().getLobbySpawn());
                }
                return true;
            } else {
                MessageManager.getInstance().sendFMessage(PrefixType.WARNING, "error.nopermission", player);
            }
        }
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw join - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.join", "Join the lobby");
    }

    @Override
    public String permission() {
        return "bw.user.join";
    }
}
