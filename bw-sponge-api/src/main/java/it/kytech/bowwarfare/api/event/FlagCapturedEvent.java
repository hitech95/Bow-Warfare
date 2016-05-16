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
package it.kytech.bowwarfare.api.event;

import it.kytech.bowwarfare.api.game.IGameSession;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.cause.Cause;

/**
 * This is fired when someone capture a flag
 */
public class FlagCapturedEvent implements Event {

    private Player flagHolder;
    private IGameSession game;
    private Cause cause;


    public FlagCapturedEvent(Player flagHolder, IGameSession game, Cause cause) {
        this.flagHolder = flagHolder;
        this.game = game;
        this.cause = cause;
    }

    public Player getPlayer() {
        return flagHolder;
    }

    public IGameSession getGame() {
        return game;
    }


    @Override
    public Cause getCause() {
        return cause;
    }
}
