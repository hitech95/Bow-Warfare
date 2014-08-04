package it.kytech.bowwarfare.commands.sub;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.commands.ISubCommand;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.MessageManager.PrefixType;
import it.kytech.bowwarfare.manager.SettingsManager;
import org.bukkit.entity.Player;

public class ForceStart implements ISubCommand {

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

        Game g = GameManager.getInstance().getGame(game);

        if (GameManager.getInstance().getGame(game).getActivePlayers() < g.getGameMode().getMinPlayer()) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notenoughtplayers", player);
            return true;
        }

        if (!g.getGameMode().requireVote()) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notrequirevote", player);
            return true;
        }

        if (g.getState() != Game.GameState.WAITING) {
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.alreadyingame", player);
            return true;
        }
        g.countdown(seconds);
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
