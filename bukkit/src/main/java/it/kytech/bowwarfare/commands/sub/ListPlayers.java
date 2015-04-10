package it.kytech.bowwarfare.commands.sub;

import it.kytech.bowwarfare.commands.ISubCommand;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;
import it.kytech.bowwarfare.manager.SettingsManager;

public class ListPlayers implements ISubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        int gid = 0;
        try {
            if (args.length == 0) {
                gid = GameManager.getInstance().getPlayerGameId(player);
            } else {
                gid = Integer.parseInt(args[0]);
            }

            String[] msg = GameManager.getInstance().getStringList(gid).split("\n");
            player.sendMessage(msg);
            return false;
        } catch (NumberFormatException ex) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notanumber", player, "input-Arena");
        } catch (NullPointerException ex) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.gamenotexist", player);
        }
        return false;
    }

    @Override
    public String help(Player p) {
        return "/list - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.listplayers", "List all players in the arena you are playing in");
    }

    @Override
    public String permission() {
        return null;
    }

}
