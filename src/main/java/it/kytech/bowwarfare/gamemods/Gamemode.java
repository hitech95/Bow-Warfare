/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.gamemods;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

/**
 *
 * @author M2K
 */
public interface Gamemode {
    
    public boolean onJoin(Player player);
    
    public boolean onPlayerKilled(Player player, boolean hasLeft);
    
    public boolean onPlayerRemove(Player player, boolean hasLeft);
    
    public boolean onArrowHit(Player attacker, Arrow arrow);
    
    public boolean onBlockBreaked(Block block, Player p);
    
    public boolean onBlockPlaced(Block block, Player p);
    
    public boolean tryLoadSpawn();
    
    public Location getRandomSpawnPoint();
    
    public int getSpawnCount();
    
    public String getGamemodeName();
    
    public void updateSingInfo(Sign s);

    public boolean isFrozenSpawn();

    public void addSpawn(Location l);
}
