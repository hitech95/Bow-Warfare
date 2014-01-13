package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

/**
 *
 * @author M2K
 */
public class DropItemEvent {

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = (Player) event.getPlayer();
        int gameid = GameManager.getInstance().getPlayerGameId(player);

        if (gameid == -1) {
            return;
        }
        event.setCancelled(true);
    }
}
