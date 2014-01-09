package it.kytech.bowwarfare.events;


import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.SettingsManager;



public class PlaceEvent implements Listener {

    public  ArrayList<Integer> allowedPlace = new ArrayList<Integer>();

    public PlaceEvent(){
        allowedPlace.addAll( SettingsManager.getInstance().getConfig().getIntegerList("block.place.whitelist"));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player p = event.getPlayer();
        int id  = GameManager.getInstance().getPlayerGameId(p);

        if(id == -1){
            int gameblockid = GameManager.getInstance().getBlockGameId(event.getBlock().getLocation());
            if(gameblockid != -1){
                if(GameManager.getInstance().getGame(gameblockid).getGameState() != Game.GameState.DISABLED){
                    event.setCancelled(true);
                }
            }
            return;
        }


        Game g = GameManager.getInstance().getGame(id);
        if(g.isPlayerinactive(p)){
            return;
        }
        if(g.getState() == Game.GameState.DISABLED){
            return;
        }
        if(g.getState() != Game.GameState.INGAME){
            event.setCancelled(true);
            return;

        }

        if(!allowedPlace.contains(event.getBlock().getTypeId())){
            event.setCancelled(true);
        }

    }
}