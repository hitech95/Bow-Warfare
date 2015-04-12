package it.kytech.bowwarfare.api;

import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.world.Location;

/**
 * Created by M2K on 12/04/2015.
 */
public interface IArena {

    int getID();

    String getName();

    String getDescription();

    String[] getAuthors();

    boolean containsPlayer(Player player);

    //boolean containsBlock(BlockLoc block); // TODO BlockLoc don't exist?

    Location firstCorner();

    Location secondCorner();
}
