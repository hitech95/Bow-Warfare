package it.kytech.bowwarfare.commands;

import org.bukkit.entity.Player;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.SettingsManager;

public class LeaveQueue implements ISubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        GameManager.getInstance().removeFromOtherQueues(player, -1);
        MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "game.playerleavequeue", player);
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw lq - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.leavequeue", "Leave the queue for any queued games");
    }

    @Override
    public String permission() {
        return null;
    }

}
