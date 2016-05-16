/**
 * This file is part of BowWarfare
 *
 * Copyright (c) 2016 hitech95 <https://github.com/hitech95>
 * Copyright (c) contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.kytech.bowwarfare;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import it.kytech.bowwarfare.api.game.manager.IArenaManager;
import it.kytech.bowwarfare.commands.*;
import it.kytech.bowwarfare.configuration.ArenaConfiguration;
import it.kytech.bowwarfare.configuration.PluginConfiguration;
import it.kytech.bowwarfare.game.manager.ArenaManager;
import it.kytech.bowwarfare.reference.Permission;
import it.kytech.bowwarfare.reference.Reference;
import it.kytech.bowwarfare.reference.Settings;
import it.kytech.bowwarfare.utils.LogHelper;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;


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
    public ArenaConfiguration arenaConfiguration;

    @Inject
    @DefaultConfig(sharedRoot = Settings.SHARED_CONFIG)
    private Path defaultConfigFile;

    @Inject
    @ConfigDir(sharedRoot = Settings.SHARED_CONFIG)
    private Path defaultConfigDir;

    @Listener
    public void preinit(GamePreInitializationEvent event) {
        logHelper = LogHelper.setup(logger, true);
        logHelper.debug("Log Helper loaded!");

        configuration = new PluginConfiguration(defaultConfigFile);
        logHelper.debug("Plugin Configuration loaded!");

        logHelper.setDebug(configuration.isDebug());

        logHelper.log("Pre-Init of BowWarfare");
    }

    @Listener
    public void Initalization(GameInitializationEvent event) {

        HashMap<List<String>, CommandSpec> subcommands = new HashMap<>();

        /*//User level commands
        subcommands.put(Arrays.asList("vote"), CommandSpec.builder()
                .permission(Permission.User.JOIN_LOBBY)
                .description(Text.of("Vote to start the game."))
                .extendedDescription(Text.of("Some game modes require a vote."))
                .executor(new Vote())
                .build());

        subcommands.put(Arrays.asList("leave"), CommandSpec.builder()
                .permission(Permission.User.JOIN_LOBBY)
                .description(Text.of("Leave the current game."))
                .extendedDescription(Text.of("Leave the current game and return to the hub or leave the queue."))
                .executor(new Leave())
                .build());

        subcommands.put(Arrays.asList("join"), CommandSpec.builder()
                .permission(Permission.User.JOIN_LOBBY)
                .description(Text.of("Join a game."))
                .extendedDescription(Text.of("If you specify the arena you will join directly!"))
                .arguments(
                        GenericArguments.optional(GenericArguments.string(Text.of("arena-slug")))
                )
                .executor(new Join())
                .build());

        subcommands.put(Arrays.asList("spectate"), CommandSpec.builder()
                .permission(Permission.User.JOIN_LOBBY)
                .description(Text.of("Become a spectator."))
                .extendedDescription(Text.of("Look the matches, you have to specify the arena."))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("arena-slug")))
                )
                .executor(new Spectate())
                .build());

        //Staff level commands
        subcommands.put(Arrays.asList("enableArena"), CommandSpec.builder()
                .permission(Permission.Staff.ENABLE_ARENA)
                .description(Text.of("Enable the specified arena."))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("arena-slug")))
                )
                .executor(new EnableArena())
                .build());

        subcommands.put(Arrays.asList("disableArena"), CommandSpec.builder()
                .permission(Permission.Staff.DISABLE_ARENA)
                .description(Text.of("Disable the specified arena."))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("arena-slug")))
                )
                .executor(new DisableArena())
                .build());

        //Admin level commands
        subcommands.put(Arrays.asList("start"), CommandSpec.builder()
                .permission(Permission.Admin.ADD_LOBBY_WALL)
                .description(Text.of("Start a game."))
                .extendedDescription(Text.of("You must be in the game, or enter the arena."))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("arena-slug")))
                )
                .executor(new ForceStart())
                .build());

        subcommands.put(Arrays.asList("setLobby", "setlobbyspawn"), CommandSpec.builder()
                .permission(Permission.Admin.SET_LOBBY_SPAWN)
                .description(Text.of("Read your inbox"))
                .extendedDescription(Text.of("Displays the server mails you received."))
                .executor(new SetLobby())
                .build());

        subcommands.put(Arrays.asList("addWall"), CommandSpec.builder()
                .permission(Permission.Admin.ADD_LOBBY_WALL)
                .description(Text.of("Add a world for the specified arena."))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("arena-slug")))
                )
                .executor(new AddWall())
                .build());

        subcommands.put(Arrays.asList("createArena"), CommandSpec.builder()
                .permission(Permission.Admin.CREATE_ARENA)
                .description(Text.of("Read your inbox"))
                .extendedDescription(Text.of("Displays the server mails you received."))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("arena-slug")))
                )
                .executor(new CreateArena())
                .build());

        subcommands.put(Arrays.asList("deleteAarena"), CommandSpec.builder()
                .permission(Permission.Admin.DELETE_ARENA)
                .description(Text.of("Delete the specified arena."))
                .arguments(
                        GenericArguments.onlyOne(GenericArguments.string(Text.of("arena-slug")))
                )
                .executor(new DeleteArena())
                .build());

        subcommands.put(Arrays.asList("reload"), CommandSpec.builder()
                .permission(Permission.Admin.RELOAD_SETTINGS)
                .description(Text.of("Reload the configuration"))
                .extendedDescription(Text.of("Displays the server mails you received."))
                .arguments(
                        GenericArguments.choices(Text.of("type"),
                                ImmutableMap.of("arena", "arena", "game", "game", "setting", "setting")
                        )
                )
                .executor(new Reload())
                .build());*/

        subcommands.put(Arrays.asList("help"), CommandSpec.builder()
                .permission(Permission.COMMAND)
                .description(Text.of("Help you to use BW commands"))
                .extendedDescription(Text.builder("Type ").color(TextColors.WHITE)
                        .append(Text.of(TextColors.GREEN, TextStyles.NONE, "/bw help <player|staff|admin>"))
                        .append(Text.builder("for command information").color(TextColors.WHITE).build()).build())
                .arguments(
                        GenericArguments.choices(Text.of("level"),
                        ImmutableMap.of("admin", "admin", "player", "player", "staff", "staff"), true)
                )
                .executor(new Help(true))
                .build());

        CommandSpec bowCommand = CommandSpec.builder()
                .description(Text.of("BowWarfare Command"))
                .permission(Permission.COMMAND)
                .children(subcommands)
                .executor(new Reload())
                .build();

        game.getCommandManager().register(this, bowCommand, "bowwarfare", "bw");
        logHelper.debug("Commands Registered!");

        arenaConfiguration = new ArenaConfiguration(defaultConfigDir);
        logHelper.debug("Loaded Arena Configuration!");

        ArenaManager arenaManager = new ArenaManager(game, arenaConfiguration);
        logHelper.debug("Loaded Arena Manager!");


        game.getServiceManager().setProvider(this, IArenaManager.class, arenaManager);
        logHelper.debug("Added the ArenaManager to Services");


        logHelper.log("Init of BowWarfare");
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logHelper.log("BowWarfare is Ready");
    }

    @Listener
    public void onServerStop(GameStoppingServerEvent event) {
        logHelper.log("BowWarfare is closed, stopping games.");
    }

}
