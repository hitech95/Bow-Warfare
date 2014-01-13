package it.kytech.bowwarfare.events;

import it.kytech.bowwarfare.SettingsManager;
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
        //TODO only on BowWarfare Worlds
        if (allowedWeather) {
            event.setCancelled(true);
        }
    }
}
