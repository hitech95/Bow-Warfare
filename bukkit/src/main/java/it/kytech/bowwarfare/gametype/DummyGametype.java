package it.kytech.bowwarfare.gametype;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.manager.MessageManager;
import it.kytech.bowwarfare.manager.SettingsManager;
import it.kytech.bowwarfare.util.NameUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scoreboard.ScoreboardManager;

@SuppressWarnings("deprecation")
public abstract class DummyGametype implements IGametype {

    public static String NAME = "DUMMY";
    public static String LONG_NAME = "DUMMY GAMETYPE";

    protected boolean isTest = false;

    protected int gameID;
    protected Game game;

    protected ArrayList<Material> allowedPlace = new ArrayList<Material>();
    protected ArrayList<Material> allowedBreak = new ArrayList<Material>();

    protected HashMap<Player, Integer> kills = new HashMap<Player, Integer>();

    protected HashMap<Block, Player> mines = new HashMap<Block, Player>();

    protected Random r = new Random();

    protected HashMap<SettingsManager.OptionFlag, Object> settings = new HashMap<SettingsManager.OptionFlag, Object>();

    protected MessageManager msgmgr = MessageManager.getInstance();
    protected ScoreboardManager sbManager = Bukkit.getScoreboardManager();

    protected void loadSettings() {
        if (!SettingsManager.getInstance().isSetGameSettings(gameID, this)) {
            loadDefaultSettings();
        } else {
            settings = SettingsManager.getInstance().getGameSettings(gameID);
        }
    }

    protected void loadDefaultSettings() {
    }

    protected void saveConfig() {
        SettingsManager.getInstance().saveGameSettings(settings, gameID);
    }

    @Override
    public boolean onPlayerRemove(Player player, boolean hasLeft) {
        return false;
    }

    @Override
    public boolean onPlayerQuit(Player p) {
        return false;
    }

    @Override
    public boolean onProjectileHit(Player attacker, Projectile pro) {
        if (pro instanceof Snowball) {
            Snowball snowball = (Snowball) pro;
            Location loc = snowball.getLocation();

            for (Player other : game.getAllPlayers()) {
                if (other.getLocation().distance(loc) <= 4 && game.isPlayerActive(other) && other != attacker) {
                    other.setLastDamageCause(new EntityDamageByEntityEvent(snowball, other, EntityDamageEvent.DamageCause.ENTITY_EXPLOSION, other.getHealth()));
                    game.killPlayer(other, attacker);
                }
            }

            loc.getWorld().createExplosion(loc, 0);

            return true;
        }
        return false;
    }

    @Override
    public String getGametypeName() {
        return NAME;
    }

    @Override
    public void updateSingInfo(Sign s) {
        s.setLine(0, NAME);
        s.setLine(1, game.getState() + "");
        s.setLine(2, game.getActivePlayers() + "/" + game.getMaxPlayer());
        s.setLine(3, "");
    }

    @Override
    public ArrayList<String> updateSignPlayer() {
        ArrayList<String> display = new ArrayList<String>();
        for (Player p : game.getAllPlayers()) {
            display.add((game.isPlayerActive(p) ? ChatColor.BLACK : ChatColor.GRAY) + NameUtil.stylize(p.getName(), true, !game.isPlayerActive(p)));
        }

        return display;
    }

    @Override
    public boolean onBlockBreaked(Block block, Player p) {
        if (block.getType() == Material.IRON_PLATE || block.getType() == Material.GOLD_PLATE) {
            if (mines.containsKey(block)) {
                mines.remove(block);
                return true;
            }
        }
        return allowedBreak.contains(block.getType());

    }

    @Override
    public boolean onBlockPlaced(Block block, Player p) {
        if (block.getType() == Material.IRON_PLATE || block.getType() == Material.GOLD_PLATE) {
            mines.put(block, p);
            return true;
        }
        return allowedPlace.contains(block.getType());
    }

    @Override
    public boolean onBlockInteract(Block block, Player p) {
        if (block.getType() == Material.IRON_PLATE || block.getType() == Material.GOLD_PLATE) {
            Player killer = mines.get(block);

            if (p == killer || killer == null) {
                return false;
            }

            for (Player other : game.getAllPlayers()) {
                if (other.getLocation().distance(block.getLocation()) <= 4 && game.isPlayerActive(other) && other != killer) {
                    other.setLastDamageCause(new EntityDamageByBlockEvent(block, other, EntityDamageEvent.DamageCause.BLOCK_EXPLOSION, other.getHealth()));
                    game.killPlayer(other, killer);
                }
            }

            block.getWorld().createExplosion(block.getLocation(), 0);

            mines.remove(block);
            block.setType(Material.AIR);
            return true;
        }
        return true;
    }

    @Override
    public boolean onGameStart() {
        return false;
    }

    @Override
    public boolean isFrozenSpawn() {
        return false;
    }

    @Override
    public int getMinPlayer() {
        return 0;
    }

    @Override
    public boolean requireVote() {
        return false;
    }
    
    @Override
    public String toString() {
        return "{name:" + NAME + ", longName:" + LONG_NAME + ", gameID:" + gameID + "}";
    }
    
    public void msgFall(MessageManager.PrefixType type, String msg, String... vars) {
        for (Player p : game.getAllPlayers()) {
            msgmgr.sendFMessage(type, msg, p, vars);
        }
    }

    public void soundFall(Sound s) {
        for (Player p : game.getAllPlayers()) {
            p.playSound(p.getLocation(), s, 10, 1);
        }
    }    
}
