package it.kytech.bowwarfare.hooks;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.util.MessageUtil;

public class HookManager {

    private static HookManager instance = new HookManager();

    private HookManager() {

    }

    public static HookManager getInstance() {
        return instance;
    }

    private HashMap<String, HookBase> hooks = new HashMap<String, HookBase>();

    public void setup() {
        hooks.put("c", new CommandHook());
    }

    /*public void runFHook(String hook, String... args){
     HashMap<String, String>vars = new HashMap<String, String>();
     for(String str: args){
     String[] s = str.split("|");
     vars.put(s[0], s[1]);
     }

     runHook(hook, vars);
     }*/
    public void runHook(String hook, String... args) {
        FileConfiguration c = SettingsManager.getInstance().getConfig();

        for (String str : c.getStringList("hooks." + hook)) {
            String[] split = str.split("!");
            String p = MessageUtil.replaceVars(split[0], args);
            String[] commands = MessageUtil.replaceVars(split[1], args).split(";");
            if (checkConditions(split[2], args)) {
                if (p.equalsIgnoreCase("console") || (split.length == 4 && Bukkit.getPlayer(p).hasPermission(split[3])) || (split.length == 3)) {
                    for (String s1 : commands) {
                        String[] s2 = s1.split("#");
                        hooks.get(s2[0]).executehook(p, s2);
                    }
                }
            }
        }
    }

    public boolean checkConditions(String str, String... args) {
        String[] C = {"<", ">", "=", ">=", "<="};
        str = str.trim();
        if (str.equalsIgnoreCase("true")) {
            return true;
        }
        for (String split : MessageUtil.replaceVars(str, args).split(";")) {

            boolean flag = false;
            for (String c : C) {
                int i = split.indexOf(c);
                if (i != -1) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                System.out.println("[BowWarfare][HookManager]Condition does not contian a compare operator: " + split);
                return false;
            }
            try {

                if (split.contains(">")) {
                    if (Double.parseDouble(split.substring(0, split.indexOf(">")).trim()) > Double.parseDouble(split.substring(split.indexOf(">")).trim())) {
                    } else {
                        return false;
                    }
                } else if (split.contains("<")) {
                    if (Double.parseDouble(split.substring(0, split.indexOf("<")).trim()) < Double.parseDouble(split.substring(split.indexOf("<")).trim())) {
                    } else {
                        return false;
                    }
                } else if (split.contains("=")) {
                    if (Double.parseDouble(split.substring(0, split.indexOf("=")).trim()) == Double.parseDouble(split.substring(split.indexOf("=")).trim())) {
                    } else {
                        return false;
                    }
                } else if (split.contains(">=")) {
                    if (Double.parseDouble(split.substring(0, split.indexOf(">=")).trim()) >= Double.parseDouble(split.substring(split.indexOf(">=")).trim())) {
                    } else {
                        return false;
                    }
                } else if (split.contains("<=")) {
                    if (Double.parseDouble(split.substring(0, split.indexOf("<=")).trim()) <= Double.parseDouble(split.substring(split.indexOf("<=")).trim())) {
                    } else {
                        return false;
                    }
                }
            } catch (Exception e) {
                System.out.println("[Bow Warfare][HookManager]Error parsing value for: " + split);
                return false;
            }
        }
        return true;

    }
}
