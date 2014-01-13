package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.GameManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

/**
 *
 * @author M2K
 */
public class ProjectileEvent implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if ((event.getEntity() instanceof Arrow)) {
            Arrow arrow = (Arrow) event.getEntity();  
            if ((arrow.getShooter() instanceof Player)) {
                Player p = (Player) arrow.getShooter();
                if (GameManager.getInstance().getPlayerGameId(p) != -1) {
                    arrow.remove();
                }
            }
        }
    }
}
