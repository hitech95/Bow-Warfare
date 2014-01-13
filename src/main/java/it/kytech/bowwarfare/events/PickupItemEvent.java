package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPickupItemEvent;

/**
 *
 * @author M2K
 */
public class PickupItemEvent {

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        if (GameManager.getInstance().getPlayerGameId(e.getPlayer()) == -1) {
            e.setCancelled(true);
        }
    }
}
