/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author M2K
 */
public class DamageEvent {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
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

        event.setCancelled(true);
        player.setHealth(player.getMaxHealth());
        player.setFoodLevel(20);
        player.setFireTicks(0);
        PlayerInventory inv = player.getInventory();
        Location l = player.getLocation();

        for (ItemStack i : inv.getContents()) {
            if (i != null) {
                l.getWorld().dropItemNaturally(l, i);
            }
        }

        GameManager.getInstance().killPlayer(player);
    }
}
