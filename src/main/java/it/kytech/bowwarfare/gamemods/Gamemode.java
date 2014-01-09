/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.gamemods;

import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;

/**
 *
 * @author M2K
 */
public interface Gamemode {
    
    public boolean onJoin(Player player);
    
    public boolean onLeave(Player player);
    
    public boolean onKill(Player killer, Player victim);
    
    public boolean onArrowHit(Player attacker, Arrow arrow);
    
    public Location getRandomSpawnPoint();
    
    public int getSpawnCount();
    
    public String getGamemodeName();
}
