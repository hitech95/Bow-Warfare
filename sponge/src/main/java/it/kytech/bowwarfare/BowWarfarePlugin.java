package it.kytech.bowwarfare;

import org.slf4j.Logger;
import com.google.inject.Inject;
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
    
    @Inject
    Logger log;

    @Subscribe
    public void onServerStart(ServerStartedEvent event) {
        log.log("Starting BowWarfare");
    }
}
