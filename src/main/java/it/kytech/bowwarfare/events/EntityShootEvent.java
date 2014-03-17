package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.GameManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.util.Vector;

/**
 *
 * @author M2K
 */
public class EntityShootEvent implements Listener {

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent e) {
        if ((e.getEntity() instanceof Player)) {
            Player p = (Player) e.getEntity();

            if (GameManager.getInstance().getPlayerGameId(p) == -1) {
                return;
            }

            if ((e.getProjectile() instanceof Arrow)) {
                Arrow arrow = (Arrow) e.getProjectile();
                arrow.setVelocity(arrow.getVelocity().multiply(new Vector(9, 9, 9)));
            }
        }
    }
}
