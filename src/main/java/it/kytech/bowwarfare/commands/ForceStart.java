package it.kytech.bowwarfare.commands;

import org.bukkit.entity.Player;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.MessageManager.PrefixType;
import it.kytech.bowwarfare.SettingsManager;

public class ForceStart implements SubCommand {

    MessageManager msgmgr = MessageManager.getInstance();

    @Override
    public boolean onCommand(Player player, String[] args) {

        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        int game = -1;
        int seconds = 10;
        if (args.length == 2) {
            seconds = Integer.parseInt(args[1]);
        }
        if (args.length >= 1) {
            game = Integer.parseInt(args[0]);

        } else {
            game = GameManager.getInstance().getPlayerGameId(player);
        }
        if (game == -1) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notingame", player);
            return true;
        }
        if (GameManager.getInstance().getGame(game).getActivePlayers() < 2) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notenoughtplayers", player);
            return true;
        }

        //TODO - Restart the game
        /*Game g = GameManager.getInstance().getGame(game);
         if (g.getState() != Game.GameState.WAITING && !player.hasPermission(permission())) {
         MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.alreadyingame", player);
         return true;
         }*/
        msgmgr.sendFMessage(PrefixType.INFO, "game.started", player, "arena-" + game);

        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw start - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.forcestart", "Forces the game to start");
    }

    @Override
    public String permission() {
        return "bw.staff.start";
    }
}
