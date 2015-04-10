package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.Game;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import it.kytech.bowwarfare.manager.GameManager;
import org.bukkit.entity.Player;

public class SignClickEvent implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void clickHandler(PlayerInteractEvent e) {

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_BLOCK) {

            Block clickedBlock = e.getClickedBlock();
            if (!(clickedBlock.getType() == Material.SIGN || clickedBlock.getType() == Material.SIGN_POST || clickedBlock.getType() == Material.WALL_SIGN)) {
                return;
            }
            Sign thisSign = (Sign) clickedBlock.getState();
            String[] lines = thisSign.getLines();
            if (lines.length < 3) {
                return;
            }
            if (lines[0].equalsIgnoreCase("[BowWarfare]")) {
                e.setCancelled(true);
                try {
                    if (lines[2].equalsIgnoreCase("Auto Assign")) {
                        GameManager.getInstance().autoAddPlayer(e.getPlayer());
                    } else {
                        String game = lines[2].replace("Arena ", "");
                        int gameno = Integer.parseInt(game);
                        GameManager.getInstance().addPlayer(e.getPlayer(), gameno);
                    }

                } catch (Exception ek) {
                }
            }

        } else if (e.getAction() == Action.PHYSICAL) {
            Block block = e.getClickedBlock();
            Player p = e.getPlayer();

            int gameID = GameManager.getInstance().getPlayerGameId(p);

            if (gameID == -1) {
                int blockgameid = GameManager.getInstance().getBlockGameId(block.getLocation());

                if (blockgameid != -1) {
                    if (GameManager.getInstance().getGame(blockgameid).getGameState() != Game.GameState.DISABLED) {
                        e.setCancelled(true);
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
                e.setCancelled(true);
                return;
            }

            if (!GameManager.getInstance().blockInteract(block, p)) {
                e.setCancelled(true);
            }

        } else {
            return;
        }

    }
}
