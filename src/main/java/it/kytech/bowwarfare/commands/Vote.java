/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.commands;

/**
 *
 * @author M2K
 */
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.SettingsManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Vote implements SubCommand {

    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return false;
        }
        int game = GameManager.getInstance().getPlayerGameId(player);
        if (game == -1) {
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, "error.notinarena", player);
            return true;
        }

        GameManager.getInstance().getGame(GameManager.getInstance().getPlayerGameId(player)).vote(player);

        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw vote - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.vote", "Votes to start the game");
    }

    @Override
    public String permission() {
        return "bw.user.vote";
    }
}
