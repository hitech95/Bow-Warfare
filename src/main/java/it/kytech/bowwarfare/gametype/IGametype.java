/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.gametype;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

/**
 *
 * @author M2K
 */
public interface IGametype {

    /**
     * @param player an Player that join the game
     * @return true if the player can join else false
     */
    public boolean onJoin(Player player);

    /**
     * @param victim an Player that get killed
     * @param killer the killer (Player)
     * @param hasLeft true if the victim left the game
     * @return true if the player is killed else false
     */
    public boolean onPlayerKilled(Player victim, Player killer, boolean hasLeft);

    public boolean onPlayerRemove(Player player, boolean hasLeft);

    public boolean onPlayerQuit(Player p);

    public void checkWin(Player victim, Player killer);

    public boolean onProjectileHit(Player attacker, Projectile pro);

    public boolean onBlockBreaked(Block block, Player p);

    public boolean onBlockPlaced(Block block, Player p);

    public boolean onBlockInteract(Block block, Player p);
    
    public boolean onGameStart();

    public boolean tryLoadSpawn();

    public Location getRandomSpawnPoint();

    public int getSpawnCount(String... args);

    public boolean isFrozenSpawn();

    public void addSpawn(Location l, String... args);

    public int getMaxPlayer();

    public int getMinPlayer();

    public void updateSingInfo(Sign s);

    public ArrayList<String> updateSignPlayer();

    public String getGametypeName();   

    public boolean requireVote();

    @Override
    public String toString();
}
