package it.kytech.bowwarfare.logging;

import java.util.HashMap;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.manager.GameManager;

public class LoggingManager implements Listener {

    public static HashMap<String, Integer> i = new HashMap<String, Integer>();

    private static LoggingManager instance = new LoggingManager();

    private LoggingManager() {

        i.put("BCHANGE", 1);
        i.put("BPLACE", 1);
        i.put("BFADE", 1);
        i.put("BBLOW", 1);
        i.put("BSTARTFIRE", 1);
        i.put("BBURN", 1);
        i.put("BREDSTONE", 1);
        i.put("LDECAY", 1);
        i.put("BPISTION", 1);

    }

    public static LoggingManager getInstance() {
        return instance;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockChanged(BlockBreakEvent e) {
        if (e.isCancelled()) {
            return;
        }
        logBlockDestoryed(e.getBlock());
        i.put("BCHANGE", i.get("BCHANGE") + 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockChanged(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }

        logBlockCreated(e.getBlock());
        i.put("BPLACE", i.get("BPLACE") + 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockChanged(BlockFadeEvent e) {
        if (e.isCancelled()) {
            return;
        }

        logBlockDestoryed(e.getBlock());
        i.put("BFADE", i.get("BFADE") + 1);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockChange(EntityExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }

        for (Block b : e.blockList()) {
            logBlockDestoryed(b);
        }

        i.put("BBLOW", i.get("BBLOW") + 1);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockChange(BlockIgniteEvent e) {
        if (e.isCancelled()) {
            return;
        }

        logBlockCreated(e.getBlock());
        i.put("BSTARTFIRE", i.get("BSTARTFIRE") + 1);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockChanged(BlockBurnEvent e) {
        if (e.isCancelled()) {
            return;
        }

        logBlockDestoryed(e.getBlock());
        i.put("BBURN", i.get("BBURN") + 1);

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockChanged(BlockGrowEvent e) {
        if (e.isCancelled()) {
            return;
        }

        logBlockCreated(e.getBlock());

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockChanged(BlockFormEvent e) {
        logBlockCreated(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void blockChanged(LeavesDecayEvent e) {
        if (e.isCancelled()) {
            return;
        }

        logBlockDestoryed(e.getBlock());
        i.put("LDECAY", i.get("LDECAY") + 1);

    }

    public void blockChange(BlockPistonExtendEvent e) {
        if (e.isCancelled()) {
            return;
        }

        for (Block b : e.getBlocks()) {
            logBlockCreated(b);
        }
        i.put("BPISTION", i.get("BPISTION") + 1);

    }

    public void logBlockCreated(Block b) {
        if (GameManager.getInstance().getBlockGameId(b.getLocation()) == -1) {
            return;
        }
        if (GameManager.getInstance().getGameState(GameManager.getInstance().getBlockGameId(b.getLocation())) == Game.GameState.DISABLED) {
            return;
        }

        QueueManager.getInstance().add(
                new BlockData(
                        GameManager.getInstance().getBlockGameId(b.getLocation()),
                        b.getWorld().getName(),
                        0,
                        (byte) 0,
                        b.getTypeId(),
                        b.getData(),
                        b.getX(),
                        b.getY(),
                        b.getZ(),
                        null)
        );
    }

    public void logBlockDestoryed(Block b) {
        if (GameManager.getInstance().getBlockGameId(b.getLocation()) == -1) {
            return;
        }
        if (GameManager.getInstance().getGameState(GameManager.getInstance().getBlockGameId(b.getLocation())) == Game.GameState.DISABLED) {
            return;
        }
        if (b.getTypeId() == 51) {
            return;
        }
        QueueManager.getInstance().add(
                new BlockData(
                        GameManager.getInstance().getBlockGameId(b.getLocation()),
                        b.getWorld().getName(),
                        b.getTypeId(),
                        b.getData(),
                        0,
                        (byte) 0,
                        b.getX(),
                        b.getY(),
                        b.getZ(),
                        null)
        );
    }

}
