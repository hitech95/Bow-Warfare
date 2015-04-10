package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.manager.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 *
 * @author M2K
 */
public class PickupItemEvent implements Listener {

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = (Player) event.getPlayer();
        int gameid = GameManager.getInstance().getPlayerGameId(player);

        if (gameid == -1) {
            return;
        }
        event.setCancelled(true);
    }
}
