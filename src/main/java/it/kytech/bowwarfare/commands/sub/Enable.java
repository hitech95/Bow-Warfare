package it.kytech.bowwarfare.commands.sub;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.Game.GameState;
import it.kytech.bowwarfare.commands.ISubCommand;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.SettingsManager;

public class Enable implements ISubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        try {
            if (args.length == 0) {
                for (Game g : GameManager.getInstance().getGames()) {
                    if (g.getState() == GameState.DISABLED) {
                        g.enable();
                    }
                }
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "game.all", player, "input-enabled");
            } else {
                GameManager.getInstance().enableGame(Integer.parseInt(args[0]));
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "game.state", player, "arena-" + args[0], "input-enabled");
            }
        } catch (NumberFormatException e) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notanumber", player, "input-Arena");
        } catch (NullPointerException e) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.gamedosentexist", player, "arena-" + args[0]);
        }
        return true;

    }

    @Override
    public String help(Player p) {
        return "/bw enable <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.enable", "Enables arena <id>");
    }

    @Override
    public String permission() {
        return "bw.staff.enable";
    }
}
