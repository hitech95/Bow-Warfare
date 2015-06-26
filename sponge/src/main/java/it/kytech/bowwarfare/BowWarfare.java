/**
 * This file is part of BowWarfare
 * <p>
 * Copyright (c) 2015 hitech95 <https://github.com/hitech95>
 * Copyright (c) contributors
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.kytech.bowwarfare;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.kytech.bowwarfare.commands.Help;
import it.kytech.bowwarfare.configuration.PluginConfiguration;
import it.kytech.bowwarfare.reference.Permission;
import it.kytech.bowwarfare.reference.Reference;
import it.kytech.bowwarfare.reference.Settings;
import it.kytech.bowwarfare.utils.LogHelper;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.event.state.PreInitializationEvent;
import org.spongepowered.api.event.state.ServerStartedEvent;
import org.spongepowered.api.event.state.ServerStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.DefaultConfig;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.command.args.GenericArguments;
import org.spongepowered.api.util.command.spec.CommandSpec;

import java.io.File;


/**
 * Main Plugin Class
 */
@Plugin(id = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION)
public class BowWarfare {

    @Inject
    public Game game;

    @Inject
    public Logger logger;
    public LogHelper logHelper;
    public PluginConfiguration configuration;
    @Inject
    @DefaultConfig(sharedRoot = Settings.SHARED_CONFIG)
    private File defaultConfigFile;
    @Inject
    @DefaultConfig(sharedRoot = Settings.SHARED_CONFIG)
    private File defaultConfigDir;

    @Subscribe
    public void preinit(PreInitializationEvent event) {
        logHelper = LogHelper.setup(logger, true);
        configuration = new PluginConfiguration(defaultConfigFile);
        logHelper.setDebug(configuration.isDebug());

        logHelper.log("Pre-Init of BowWarfare");
    }

    @Subscribe
    public void Initalization(InitializationEvent event) {


        CommandSpec bowCommand = CommandSpec.builder()
                .description(Texts.of("BowWarfare Command"))
                .permission(Permission.COMMAND)
                .arguments(
                        GenericArguments.optional(GenericArguments.string(Texts.of("help"))),
                        GenericArguments.optional(GenericArguments.choices(Texts.of("level"), ImmutableMap.of("admin", "player", "staff", "list")))
                )
                .executor(new Help())
                .build();

        game.getCommandDispatcher().register(this, bowCommand, "bowwarfare", "bw");
        logHelper.log("Init of BowWarfare");
    }

    @Subscribe
    public void onServerStart(ServerStartedEvent event) {
        logHelper.log("BowWarfare is Ready");
    }

    @Subscribe
    public void onServerStop(ServerStoppingEvent event) {
        logHelper.log("BowWarfare is closed, stopping games.");
    }

}
