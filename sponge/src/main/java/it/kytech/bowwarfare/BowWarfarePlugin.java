package it.kytech.bowwarfare;

import it.kytech.bowwarfare.reference.Reference;
import it.kytech.bowwarfare.utils.LogHelper;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.plugin.Plugin;


/**
 * Main Plugin Class
 */
@Plugin(id = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class BowWarfarePlugin {

    @Subscribe
    public void onServerStart(ServerStartedEvent event) {
        LogHelper.getInstance().log("Starting BowWarfare");
    }
}
