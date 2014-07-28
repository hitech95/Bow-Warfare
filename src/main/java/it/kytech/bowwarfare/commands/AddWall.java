package it.kytech.bowwarfare.commands;

import org.bukkit.entity.Player;
import it.kytech.bowwarfare.manager.LobbyManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.SettingsManager;

public class AddWall implements ISubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        } else if (args.length < 1) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notspecified", player, "input-Arena");
            return true;
        }
        LobbyManager.getInstance().setLobbySignsFromSelection(player, Integer.parseInt(args[0]));
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw addwall <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.addwall", "Add a lobby stats wall for Arena <id>");
    }

    @Override
    public String permission() {
        return "bw.admin.addwall";
    }
}
