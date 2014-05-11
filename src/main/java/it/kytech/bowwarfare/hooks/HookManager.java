package it.kytech.bowwarfare.hooks;

import it.kytech.bowwarfare.SettingsManager;
import it.kytech.bowwarfare.util.MessageUtil;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

public class HookManager {

    private static HookManager instance = new HookManager();

    private HookManager() {

    }

    public static HookManager getInstance() {
        return instance;
    }

    private Map<Class<? extends HookBase>, HookBase> hooks = new HashMap<>();

	@SuppressWarnings("unchecked")
	public <T extends HookBase> T getHook(Class<T> clazz) {
    	for (HookBase hbase : hooks.values()) {
    		if (hbase.getClass() == clazz) {
    			try {
    				return (T) hbase;
    			} catch (Throwable t) {}
    		}
    	}
    	return null;
    }
    
    public void setup() {
        hooks.put(CommandHook.class, new CommandHook());
        hooks.put(EconHook.class, new EconHook());
    }
    
    public void runHook(String hook, String... args) {
    	for (Class<? extends HookBase> clazz : hooks.keySet()) {
    		if (hooks.get(clazz).getShortName().equalsIgnoreCase(hook)) {
    			runHook(clazz, args);
    		}
    	}
    }
    
    public void runHook(Class<? extends HookBase> hook, String... args) {
        FileConfiguration c = SettingsManager.getInstance().getConfig();

        HookBase hbase = hooks.get(hook);
        
        if (c.getBoolean("hooks." + hbase.getShortName(), true) && hbase.isReady()) {
            if (args.length < hbase.getParameters().length) {
            	boolean go = true;
            	Class<?>[] params = hbase.getParameters();
            	for (int i = 0; i < args.length; i++) {
            		if (params[i] != Wildcard.class && args[i].getClass() != params[i] && !args[i].getClass().isAssignableFrom(params[i])) {
            			go = false;
            			break;
            		}
            	}
            	if (go) {
            		hbase.executeHook(args);
            	}
            }
        }
        
        /*
         * for (String str : c.getStringList("hooks." + hook)) {
         *     String[] split = str.split("!");
         *     String p = MessageUtil.replaceVars(split[0], args);
         *     String[] commands = MessageUtil.replaceVars(split[1], args).split(";");
         *     if (checkConditions(split[2], args)) {
         *         if (p.equalsIgnoreCase("console") || (split.length == 4 && Bukkit.getPlayer(p).hasPermission(split[3])) || (split.length == 3)) {
         *             for (String s1 : commands) {
         *                 String[] s2 = s1.split("#");
         *                 hooks.get(s2[0]).executeHook(p, s2);
         *             }
         *         }
         *     }
         * }
         */
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
