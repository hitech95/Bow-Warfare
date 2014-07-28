package it.kytech.bowwarfare.manager;

import it.kytech.bowwarfare.hooks.HookManager;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyManager {

    private static EconomyManager instance = new EconomyManager();
    private Economy economy;
    private boolean enabled = false;

    public static double win = 0;
    public static double loose = 0;
    public static double kill = 0;
    public static double death = 0;

    private EconomyManager() {

    }

    public static EconomyManager getInstance() {
        return instance;
    }

    public void setup() {
        enabled = setupEconomy();

        if (enabled) {
            win = SettingsManager.getInstance().getConfig().getDouble("economy.win");
            loose = SettingsManager.getInstance().getConfig().getDouble("economy.loose");
            kill = SettingsManager.getInstance().getConfig().getDouble("economy.kill");
            death = SettingsManager.getInstance().getConfig().getDouble("economy.death");
        }
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }

    public Economy getEcon() {
        return economy;
    }

    public boolean econPresent() {
        return enabled;
    }

    public boolean executeTask(double price, Player p) {
        String command = (price < 0) ? "REMOVE" : "ADD";
        return HookManager.getInstance().runHook("economy", new String[]{command, p.getName(), "" + price});
    }
}
