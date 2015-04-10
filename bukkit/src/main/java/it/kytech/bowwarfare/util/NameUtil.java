package it.kytech.bowwarfare.util;

import org.bukkit.ChatColor;
import it.kytech.bowwarfare.BowWarfare;

public class NameUtil {

    public static String stylize(String name, boolean s, boolean r) {

        if (BowWarfare.auth.contains(name) && r) {
            name = ChatColor.UNDERLINE + name;
        }
        if (BowWarfare.auth.contains(name) && !r) {
            name = ChatColor.BOLD + name;
        }
        if (s && name.equalsIgnoreCase("Double0negative")) {
            name = name.replace("Double0negative", "Double0");
        }
        return name;
    }
}
