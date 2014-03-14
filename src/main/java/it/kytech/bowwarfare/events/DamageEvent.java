/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author M2K
 */
public class DamageEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        Player killer = null;

        System.out.println("Damaged Player ---------->" + player.getDisplayName());
        int gameid = GameManager.getInstance().getPlayerGameId(player);

        if (gameid == -1) {
            return;
        }

        if (!GameManager.getInstance().isPlayerActive(player)) {
            return;
        }

        Game game = GameManager.getInstance().getGame(gameid);

        if (game.getState() != Game.GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        if ((event.getDamager() instanceof Projectile)) {
            Projectile projectile = (Projectile) event.getDamager();
            if (((projectile.getShooter() instanceof Player)) && ((projectile instanceof Arrow))) {
                killer = (Player) projectile.getShooter();
            } else {
                event.setCancelled(true);
                return;
            }
        } else if ((event.getDamager() instanceof Player)) {
            killer = (Player) event.getDamager();
            event.setDamage(event.getDamage() * 7);
        } else {
            event.setCancelled(true);
            return;
        }

        if (GameManager.getInstance().getPlayerGameId(killer) == -1 || GameManager.getInstance().getPlayerGameId(killer) != gameid) {
            event.setCancelled(true);
            return;
        }

        if (player.getHealth() <= event.getDamage()) {
            event.setCancelled(true);
            player.setHealth(player.getMaxHealth());
            player.setFoodLevel(20);
            player.setFireTicks(0);
            PlayerInventory inv = player.getInventory();
            Location l = player.getLocation();

            /*for (ItemStack i : inv.getContents()) {
             if (i != null) {
             l.getWorld().dropItemNaturally(l, i);
             }
             }*/
            player.setLastDamageCause(event);
            GameManager.getInstance().killPlayer(player, killer);

        }
    }
}
