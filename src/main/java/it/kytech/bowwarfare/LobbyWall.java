package it.kytech.bowwarfare;

import it.kytech.bowwarfare.manager.GameManager;
import it.kytech.bowwarfare.manager.LobbyManager;
import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

public class LobbyWall {

    private ArrayList< Sign> signs = new ArrayList< Sign>();
    private ArrayList< String> msgqueue = new ArrayList< String>();
    private int gameid;

    public LobbyWall(int gid) {
        gameid = gid;
    }

    public boolean loadSign(World w, int x1, int x2, int z1, int z2, int y1) {
        boolean usingx = (x1 != x2);
        BowWarfare.debug(w + " " + x1 + " " + x2 + " " + z1 + " " + z2 + " " + y1 + " " + usingx);
        int dir = new Location(w, x1, y1, z1).getBlock().getData();
        if (usingx) {
            for (int a = Math.max(x1, x2); a >= Math.min(x1, x2); a--) {
                Location l = new Location(w, a, y1, z1);
                BlockState b = l.getBlock().getState();
                if (b instanceof Sign) {
                    signs.add((Sign) b);
                    LobbyManager.lobbychunks.add(b.getChunk());
                    BowWarfare.debug("usingx - " + b.getLocation().toString());
                } else {
                    BowWarfare.debug("Not a sign" + b.getType().toString());
                    return false;
                }
            }
        } else {
            for (int a = Math.min(z1, z2); a <= Math.max(z1, z2); a++) {
                BowWarfare.debug(a);
                Location l = new Location(w, x1, y1, a);
                BlockState b = l.getBlock().getState();
                if (b instanceof Sign) {
                    signs.add((Sign) b);
                    LobbyManager.lobbychunks.add(b.getChunk());
                    BowWarfare.debug("notx - " + b.getLocation().toString());
                } else {
                    BowWarfare.debug("Not a sign" + b.getType().toString());
                    return false;
                }
            }
        }
        BowWarfare.debug("dir: " + dir);
        if (dir == 3 || dir == 5) {
            Collections.reverse(signs);
        }
        addMsg("BowWarfare");
        addMsg("hitech95");
        addMsg("Game id: " + gameid);
        update();
        return true;
    }

    public void update() {
        if (msgqueue.size() > 0) {
            display();
            Bukkit.getScheduler().scheduleSyncDelayedTask(GameManager.getInstance().getPlugin(), new Runnable() {
                @Override
                public void run() {
                    display();
                    update();
                }
            }, 20L);
            return;
        }

        clear();
        Game game = GameManager.getInstance().getGame(gameid);
        Sign s0 = signs.get(0);
        Sign s1 = signs.get(1);

        //static arena info
        s0.setLine(0, "[BowWarfare]");
        s0.setLine(1, "Click to join");
        s0.setLine(2, "Arena " + gameid);
        s0.setLine(3, game.getName());

        //live arena data
        if (game.getGameMode() != null) {
            game.getGameMode().updateSingInfo(s1);

            if (game.getState() == Game.GameState.RESETING || game.getState() == Game.GameState.FINISHING) {
                for (int i = 0; i < 4; i++) {
                    s1.setLine(i, "");
                }

                s1.setLine(3, game.getRBStatus());
                if (game.getRBPercent() > 100) {
                    s1.setLine(1, "Saving Queue");
                    s1.setLine(3, (int) game.getRBPercent() + " left");
                } else {
                    s1.setLine(3, (int) game.getRBPercent() + "%");
                }
            }
        } else {
            s1.setLine(0, "");
            s1.setLine(1, game.getState() + "");
            s1.setLine(2, "");
            s1.setLine(3, "");
        }

        //live player data
        ArrayList< String> display = new ArrayList< String>();

        if (game.getGameMode() != null) {
            display = game.getGameMode().updateSignPlayer();
        }

        try {
            int no = 2;
            int line = 0;
            for (String s : display) {
                signs.get(no).setLine(line, s);
                line++;
                if (line >= 4) {
                    line = 0;
                    no++;
                }
            }
        } catch (IndexOutOfBoundsException e) {
        }
        for (Sign s : signs) {
            s.update();
        }
    }

    public void clear() {
        for (Sign s : signs) {
            for (int a = 0; a < 4; a++) {
                s.setLine(a, "");
            }
            s.update();
        }
    }

    public void addMsg(String s) {
        msgqueue.add(s);
    }
    int displaytid = 0;

    public void display() {
        int a = 0;
        while (msgqueue.size() > 0 && a < 4) {
            String s = msgqueue.get(0);
            for (int b = 0; b < s.length() / 16; b++) {
                try {
                    signs.get(b).setLine(a, s.substring(b * 16, (b + 1) * 16));

                    signs.get(b).update();
                } catch (IndexOutOfBoundsException e) {
                }
            }
            a++;
            msgqueue.remove(0);
        }

    }
    int aniline = 0;
}
