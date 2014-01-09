package it.kytech.bowwarfare.commands;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.LobbyManager;
import it.kytech.bowwarfare.MessageManager;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.MessageManager.PrefixType;



public class DelArena implements SubCommand{

    @Override
    public boolean onCommand(Player player, String[] args) {
        if (!player.hasPermission(permission()) && !player.isOp()){
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.nopermission", player);
            return true;
        }
        
        if(args.length != 1){
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.notspecified", player, "input-Arena");
            return true;
        }
        
        FileConfiguration s = SettingsManager.getInstance().getSystemConfig();
        //FileConfiguration spawn = SettingsManager.getInstance().getSpawns();
        int arena = Integer.parseInt(args[0]);
        Game g = GameManager.getInstance().getGame(arena);
        
        if(g == null){
            MessageManager.getInstance().sendFMessage(PrefixType.ERROR, "error.gamedoesntexist", player, "arena-" + arena);
            return true;
        }
        
        g.disable();
        s.set("bw-system.arenas."+arena+".enabled", false);
        s.set("bw-system.arenano", s.getInt("bw-system.arenano") - 1);
        //spawn.set("spawns."+arena, null);
        MessageManager.getInstance().sendFMessage(PrefixType.INFO, "info.deleted", player, "input-Arena");
        SettingsManager.getInstance().saveSystemConfig();
        GameManager.getInstance().hotRemoveArena(arena);
        //LobbyManager.getInstance().clearAllSigns();
        LobbyManager.getInstance().removeSignsForArena(arena);
        return true;
    }

    @Override
    public String help (Player p) {
        return "/bw delarena <id> - " + SettingsManager.getInstance().getMessageConfig().getString("messages.help.delarena", "Delete an arena");
    }

	@Override
	public String permission() {
		return "bw.admin.deletearena";
	}   
}