/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.kytech.bowwarfare.gametype;

import java.util.ArrayList;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

/**
 *
 * @author M2K
 */
public interface Gametype {

    public boolean onJoin(Player player);

    public boolean onPlayerKilled(Player victim, Player killer, boolean hasLeft);

    public boolean onPlayerRemove(Player player, boolean hasLeft);

    public boolean onProjectileHit(Player attacker, Projectile pro);

    public boolean onBlockBreaked(Block block, Player p);

    public boolean onBlockPlaced(Block block, Player p);

    public boolean onBlockInteract(Block block, Player p);

    public boolean tryLoadSpawn();
    
    public void checkWin(Player victim, Player killer);

    public Location getRandomSpawnPoint();

    public int getSpawnCount(String... args);

    public int getMaxPlayer();

    public int getMinPlayer();

    public String getGametypeName();

    public void updateSingInfo(Sign s);

    public ArrayList<String> updateSignPlayer();

    public boolean isFrozenSpawn();

    public void addSpawn(Location l, String... args);

    public boolean onPlayerQuit(Player p);

    @Override
    public String toString();
}
