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

    private EconomyManager() {
    }

    public static EconomyManager getInstance() {
        return instance;
    }

    public void setup() {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        enabled = (economy != null);
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
