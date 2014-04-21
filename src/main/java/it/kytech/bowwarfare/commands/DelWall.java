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
import it.kytech.bowwarfare.LobbyManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.SettingsManager;
import org.bukkit.entity.Player;

public class DelWall implements SubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        LobbyManager.getInstance().deleteLobbySignsFromSelection(player);
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw delwall <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.delwall", "Delete the selected lobby stats wall");
    }

    @Override
    public String permission() {
        return "bw.admin.delwall";
    }
}
