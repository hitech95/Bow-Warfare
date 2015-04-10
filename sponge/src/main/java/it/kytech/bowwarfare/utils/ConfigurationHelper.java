package it.kytech.bowwarfare.utils;

import com.google.inject.Inject;
import it.kytech.bowwarfare.reference.Settings;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.service.config.DefaultConfig;

import java.io.File;

/**
 * Created by M2K on 10/04/2015.
 */
public class ConfigurationHelper {

    private static ConfigurationHelper instance;

    @Inject
    @DefaultConfig(sharedRoot = Settings.SHARED_CONFIG)
    private File defaultConfig;

    @Inject
    @DefaultConfig(sharedRoot = Settings.SHARED_CONFIG)
    private ConfigurationLoader<CommentedConfigurationNode> configManager;

    public static ConfigurationHelper getInstance() {
        if (instance == null) {
            instance = new ConfigurationHelper();
        }

        return instance;
    }

    public boolean isDebug() {
        return true; //TODO read config from file
    }
}
