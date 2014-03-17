package it.kytech.bowwarfare.commands;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.SettingsManager;

public class Leave implements SubCommand {

    public boolean onCommand(Player player, String[] args) {
        if (GameManager.getInstance().getPlayerGameId(player) == -1) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notinarena", player);
        } else {
            GameManager.getInstance().removePlayer(player);
        }
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw leave - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.leave", "Leaves the game");
    }

    @Override
    public String permission() {
        return null;
    }
}
