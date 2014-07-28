package it.kytech.bowwarfare.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import it.kytech.bowwarfare.BowWarfare;
import it.kytech.bowwarfare.manager.GameManager;

public class UpdateChecker {

    //Includes simple metrics!!
    public void check(Player player, Plugin p) {

        String response = "";
        String data = "";

        String v = p.getDescription().getVersion();
        String ip = Bukkit.getIp();
        int port = Bukkit.getPort();
        int arenas = GameManager.getInstance().getGameCount();

        try {
            //IP and PORT used so data is unique so amount of servers using the plugin can be calculated correctly
            data = URLEncoder.encode("version", "UTF-8") + "=" + URLEncoder.encode(v, "UTF-8");
            data += "&" + URLEncoder.encode("ip", "UTF-8") + "=" + URLEncoder.encode(ip, "UTF-8");
            data += "&" + URLEncoder.encode("port", "UTF-8") + "=" + URLEncoder.encode("" + port, "UTF-8");
            data += "&" + URLEncoder.encode("a", "UTF-8") + "=" + URLEncoder.encode("" + arenas, "UTF-8");

            URL url = new URL("http://kytech.it/plugins/BowWarfare/updatecheck.php");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write(data);
            wr.flush();

            // Get The Response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response = line;
            }

            String[] in = response.split("~");
            Boolean b = Boolean.parseBoolean(in[0]);

            String build = v.split("\\.")[(v.split("\\.")).length - 1];

            if (!b) {
                player.sendMessage(ChatColor.DARK_BLUE + "--------------------------------------");
                player.sendMessage(ChatColor.DARK_RED + "[BowWarfare] Update Available!");
                player.sendMessage(ChatColor.DARK_AQUA + "Your Build is: " + ChatColor.GOLD + build + ChatColor.DARK_AQUA + " Latest: " + ChatColor.GOLD + in[1]);
                //player.sendMessage(ChatColor.DARK_AQUA + in[2]);
                //player.sendMessage(ChatColor.AQUA + "" + ChatColor.UNDERLINE + in[3]);
                player.sendMessage(ChatColor.DARK_BLUE + "--------------------------------------");
                BowWarfare.$("[BW][Info] Updates found!");

            } else {
                BowWarfare.$("[BW][Info] No updates found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            BowWarfare.$(Level.WARNING, "[BW] Could not check for updates.");
        }
    }
}
