package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.manager.GameManager;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
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

        Projectile entity = event.getEntity();

        if ((entity.getShooter() instanceof Player)) {
            Player player = (Player) entity.getShooter();

            int gameid = GameManager.getInstance().getPlayerGameId(player);

            if (gameid == -1) {
                return;
            }

            if (!GameManager.getInstance().isPlayerActive(player)) {
                return;
            }

            Game game = GameManager.getInstance().getGame(gameid);

            if (game.getState() == Game.GameState.INGAME) {
                GameManager.getInstance().projectileHit(player, entity);
            }

        }

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
