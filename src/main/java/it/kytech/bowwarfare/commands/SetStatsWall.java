package it.kytech.bowwarfare.commands;

import org.bukkit.entity.Player;
import it.kytech.bowwarfare.manager.SettingsManager;

public class SetStatsWall implements ISubCommand {

    @Override
    public boolean onCommand(Player player, String[] args) {
        //StatsWallManager.getInstance().setStatsSignsFromSelection(player);
        return false;
    }

    public String help(Player p) {
        return "/bw setstatswall - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.setstatswall", "Sets the stats wall");
    }

    @Override
    public String permission() {
        return null;
    }
}
