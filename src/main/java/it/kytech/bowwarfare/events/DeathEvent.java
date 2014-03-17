package it.kytech.bowwarfare.events;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK;
import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.PROJECTILE;

public class DeathEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDieEvent(EntityDamageEvent event) {
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
        System.out.println(event.getCause().toString());

        if (event.getCause() == ENTITY_ATTACK || event.getCause() == PROJECTILE) {
            return;
        }

        if (GameManager.getInstance().isInKitMenu(player)) {
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
            GameManager.getInstance().killPlayer(player, null);
        }
    }
}
