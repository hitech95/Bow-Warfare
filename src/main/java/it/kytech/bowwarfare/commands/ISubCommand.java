package it.kytech.bowwarfare.commands;

import org.bukkit.entity.Player;

public interface ISubCommand {

    public boolean onCommand(Player player, String[] args);

    public String help(Player p);

    public String permission();

}
