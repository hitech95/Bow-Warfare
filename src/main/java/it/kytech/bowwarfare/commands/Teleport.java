package it.kytech.bowwarfare.commands;

import it.kytech.bowwarfare.GameManager;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.MessageManager.PrefixType;
import it.kytech.bowwarfare.SettingsManager;

public class Teleport implements SubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (player.hasPermission(permission())) {
            if (args.length == 1) {
                try {
                    int a = Integer.parseInt(args[0]);
                    try {
                        player.teleport(GameManager.getInstance().getGame(a).getGameMode().getRandomSpawnPoint());
                    } catch (Exception e) {
                        MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.nospawns", player);
                    }
                } catch (NumberFormatException e) {
                    MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notanumber", player, "input-" + args[0]);
                }
            } else {
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notspecified", player, "input-Game ID");
            }
        } else {
            MessageManager.getInstance().sendFMessage(PrefixType.WARNING, "error.nopermission", player);
        }
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw tp <arenaid> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.teleport", "Teleport to an arena");
    }

    @Override
    public String permission() {
        return "bw.arena.teleport";
    }

}
