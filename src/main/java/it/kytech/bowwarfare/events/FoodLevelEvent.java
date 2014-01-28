package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 *
 * @author M2K
 */
public class FoodLevelEvent implements Listener{
    
    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if ((e.getEntity() instanceof Player)) {
            Player p = (Player) e.getEntity();
            if (GameManager.getInstance().getPlayerGameId(p) != -1) {
                e.setCancelled(true);
                p.setFoodLevel(20);
            }
        }
    }
}
