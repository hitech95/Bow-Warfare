package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.Game;
import it.kytech.bowwarfare.GameManager;
import it.kytech.bowwarfare.SettingsManager;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 *
 * @author M2K
 */
public class WeatherEvent implements Listener {

    public boolean allowedWeather = false;

    public WeatherEvent() {
        allowedWeather = SettingsManager.getInstance().getConfig().getBoolean("weather-allowed");
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        World w = event.getWorld();
        for (Game g : GameManager.getInstance().getGames()) {
            if (g.getWorld().equals(w) && !allowedWeather) {
                event.setCancelled(true);
            }
        }
    }
}
