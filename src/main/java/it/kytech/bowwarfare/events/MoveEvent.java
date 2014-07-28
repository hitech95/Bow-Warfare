package it.kytech.bowwarfare.events;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.Game.GameState;

public class MoveEvent implements Listener {

    HashMap<Player, Vector> playerpos = new HashMap<Player, Vector>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void frozenSpawnHandler(PlayerMoveEvent e) {

        if (GameManager.getInstance().getPlayerGameId(e.getPlayer()) == -1) {
            playerpos.remove(e.getPlayer());
            return;
        }

        int gameID = GameManager.getInstance().getPlayerGameId(e.getPlayer());

        if (!GameManager.getInstance().isFrozenSpawn(gameID)) {
            return;
        }

        if (GameManager.getInstance().getGameState(gameID) == Game.GameState.INGAME) {
            return;
        }

        GameState state = GameManager.getInstance().getGameState(gameID);

        if (GameManager.getInstance().isPlayerActive(e.getPlayer()) && state != Game.GameState.INGAME) {
            if (playerpos.get(e.getPlayer()) == null) {
                playerpos.put(e.getPlayer(), e.getPlayer().getLocation().toVector());
                return;
            }
            Location l = e.getPlayer().getLocation();
            Vector v = playerpos.get(e.getPlayer());
            if (l.getBlockX() != v.getBlockX() || l.getBlockZ() != v.getBlockZ()) {
                l.setX(v.getBlockX() + .5);
                l.setZ(v.getBlockZ() + .5);
                l.setYaw(e.getPlayer().getLocation().getYaw());
                l.setPitch(e.getPlayer().getLocation().getPitch());
                e.getPlayer().teleport(l);
            }
        }
    }
}
