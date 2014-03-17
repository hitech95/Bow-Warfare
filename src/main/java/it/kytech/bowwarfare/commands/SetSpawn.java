package it.kytech.bowwarfare.commands;

import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.SpawnManager;
import it.kytech.bowwarfare.gametype.Gametype;
import java.util.Arrays;

public class SetSpawn implements SubCommand {

    HashMap<Integer, Integer> next = new HashMap<Integer, Integer>();

    public SetSpawn() {
    }

    public void loadNextSpawn(String gamemode) {
        for (Game g : GameManager.getInstance().getGames().toArray(new Game[0])) { //Avoid Concurrency problems
            Gametype availableGameMode = g.getAvailableGameMode(gamemode);
            if (availableGameMode == null) {
                next.put(g.getID(), 1);
            } else {
                next.put(g.getID(), availableGameMode.getSpawnCount() + 1);
            }
        }
    }

    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        Location l = player.getLocation();
        int game = GameManager.getInstance().getBlockGameId(l);
        if (game == -1) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notinarena", player);
            return true;
        }

        if (args.length < 2) {
            MessageManager.getInstance().sendMessage(MessageManager.PrefixType.ERROR, help(player), player);
            return true;
        }

        loadNextSpawn(args[1]);

        int i = 0;
        if (args[0].equalsIgnoreCase("next")) {
            i = next.get(game);
            next.put(game, next.get(game) + 1);
        } else {
            try {
                i = Integer.parseInt(args[0]);
                if (i > next.get(game) + 1 || i < 1) {
                    MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.between", player, "num-" + next.get(game));
                    return true;
                }
                if (i == next.get(game)) {
                    next.put(game, next.get(game) + 1);
                }
            } catch (Exception e) {
                MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.badinput", player);
                return false;
            }
        }
        if (i == -1) {
            MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.ERROR, "error.notinside", player);
            return true;
        }
        if (args.length > 2) {
            SpawnManager.getInstance().setSpawn(l, i, game, args[1], Arrays.copyOfRange(args, 2, args.length));
        } else {
            SpawnManager.getInstance().setSpawn(l, i, game, args[1]);
        }
        MessageManager.getInstance().sendFMessage(MessageManager.PrefixType.INFO, "info.spawnset", player, "num-" + i, "arena-" + game);
        return true;
    }

    @Override
    public String help(Player p) {
        return "/bw setspawn next <GameType> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.setspawn", "Sets a spawn for the arena you are located in");
    }

    @Override
    public String permission() {
        return "bw.admin.setarenaspawns";
    }
}
