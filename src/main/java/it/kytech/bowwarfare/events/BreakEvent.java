package it.kytech.bowwarfare.events;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.SettingsManager;
import org.bukkit.block.Block;

public class BreakEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player p = event.getPlayer();
        Block block = event.getBlock();
        int gameID = GameManager.getInstance().getPlayerGameId(p);

        if (gameID == -1) {
            int blockgameid = GameManager.getInstance().getBlockGameId(block.getLocation());

            if (blockgameid != -1) {
                if (GameManager.getInstance().getGame(blockgameid).getGameState() != Game.GameState.DISABLED) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        Game g = GameManager.getInstance().getGame(gameID);

        if (g.isPlayerinactive(p)) {
            return;
        }
        if (g.getState() == Game.GameState.DISABLED) {
            return;
        }
        if (g.getState() != Game.GameState.INGAME) {
            event.setCancelled(true);
            return;
        }

        if (!GameManager.getInstance().blockBreak(block, p)) {
            event.setCancelled(true);
        }
    }
}
