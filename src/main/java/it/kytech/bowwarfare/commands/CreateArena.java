package it.kytech.bowwarfare.commands;

import org.bukkit.entity.Player;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.SettingsManager;

public class CreateArena implements ISubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        GameManager.getInstance().createArenaFromSelection(player);
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw createarena - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.createarena", "Create a new arena with the current WorldEdit selection");
    }

    @Override
    public String permission() {
        return "bw.admin.createarena";
    }
}
