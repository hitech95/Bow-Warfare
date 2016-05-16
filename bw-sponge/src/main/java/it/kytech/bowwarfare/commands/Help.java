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
package it.kytech.bowwarfare.commands;

import it.kytech.bowwarfare.reference.Reference;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.CommandBlockSource;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

/**
 * Created by Hitech95 on 25/06/2015.
 */
public class Help implements CommandExecutor {

    //private HashMap<String, Integer> helpinfo;
    private boolean isSub;

    public Help() {
        this(false);
    }

    public Help(boolean isSub) {
        this.isSub = isSub;
        //helpinfo = new HashMap<String, Integer>();
        //loadHelpInfo();
    }

    private void loadHelpInfo() {
        //helpinfo.put("join", 1);
        //helpinfo.put("vote", 1);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof CommandBlockSource) {
            throw new CommandException(Text.builder("Hi Companion Cube! But you cant send commands to me! <3")
                    .color(TextColors.DARK_RED).build());
        }

        if (isSub && args.<String>getOne("level").isPresent()) {
            String level = args.<String>getOne("level").get().toLowerCase();
            switch (level) {
                case "player":
                    printHelp(src, 1);
                    break;
                case "staff":
                    printHelp(src, 2);
                    break;
                case "admin":
                    printHelp(src, 3);
                    break;
                default:
                    src.sendMessage(Text.builder(
                            "\"" + level + "\" is not a valid page! Valid pages are [player], [staff], [admin]."
                    ).color(TextColors.GOLD).build());
            }
        } else {
            src.sendMessage(Text.builder("========== " + Reference.MOD_NAME + " Version: " + Reference.MOD_VERSION + " ==========")
                    .color(TextColors.DARK_GREEN).build());

            src.sendMessage(Text.builder("Type ").color(TextColors.WHITE)
                    .append(Text.of(TextColors.GREEN, TextStyles.NONE, "/bw help <player|staff|admin>"))
                    .append(Text.builder("for command information").color(TextColors.WHITE).build()).build());
        }

        return CommandResult.success();
    }

    public void printHelp(CommandSource src, int page) {
        String headerValue = "Commands";

        if (page == 1) {
            headerValue = "Player Commands";
        }
        if (page == 2) {
            headerValue = "Staff Commands";
        }
        if (page == 3) {
            headerValue = "Admin Commands";
        }

        Text header = Text.builder("============ ").color(TextColors.DARK_GREEN).append(
                Text.builder(headerValue).color(TextColors.GREEN).build()).append(
                Text.builder(" ============").color(TextColors.DARK_GREEN).build()).build();
        src.sendMessage(header);
    }
}
