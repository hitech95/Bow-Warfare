/**
 * This file is part of BowWarfare
 * <p/>
 * Copyright (c) 2015 hitech95 <https://github.com/hitech95>
 * Copyright (c) contributors
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.kytech.bowwarfare.commands;

import it.kytech.bowwarfare.reference.Reference;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.source.CommandBlockSource;
import org.spongepowered.api.util.command.source.ConsoleSource;
import org.spongepowered.api.util.command.spec.CommandExecutor;

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
        if (src instanceof ConsoleSource) {
            throw new CommandException(Texts.builder("Hello GLaDOS! But only in-game players can use BowWafare's commands!")
                    .color(TextColors.DARK_RED).build());
        } else if (src instanceof CommandBlockSource) {
            throw new CommandException(Texts.builder("Hi Companion Cube! But you cant send commands to me! <3")
                    .color(TextColors.DARK_RED).build());
        }

        if ((isSub || args.<String>getOne("help").isPresent()) && !args.<String>getOne("level").isPresent()) {

            String command = (isSub) ? "help" : args.<String>getOne("help").get().toLowerCase();
            String level = args.<String>getOne("level").get().toLowerCase();

            if (command.equals("help")) {
                switch (level) {
                    case "player":
                        help(src, 1);
                        break;
                    case "staff":
                        help(src, 2);
                        break;
                    case "admin":
                        help(src, 3);
                        break;
                    default:
                        src.sendMessage(Texts.builder(
                                "\"" + level + "\" is not a valid page! Valid pages are [player], [staff], [admin]."
                        ).color(TextColors.GOLD).build());
                }
            }
            return CommandResult.success();

        }
        src.sendMessage(Texts.builder(Reference.MOD_NAME + " Version: " + Reference.MOD_VERSION)
                .color(TextColors.DARK_RED).build());
        src.sendMessage(Texts.builder("Type /bw help <player | staff | admin> for command information"
        ).color(TextColors.DARK_RED).build());

        return CommandResult.success();
    }

    public void help(CommandSource src, int page) {
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

        Text header = Texts.builder("------------ ").color(TextColors.BLUE).append(
                Texts.builder(headerValue).color(TextColors.DARK_AQUA).build()).append(
                Texts.builder(" ------------").color(TextColors.BLUE).build()).build();
        src.sendMessage(header);
    }
}
