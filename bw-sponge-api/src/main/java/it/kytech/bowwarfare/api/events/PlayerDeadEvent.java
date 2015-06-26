/**
 * This file is part of BowWarfare
 *
 * Copyright (c) 2015 hitech95 <https://github.com/hitech95>
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
package it.kytech.bowwarfare.api.events;

import it.kytech.bowwarfare.api.game.IGameSession;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.AbstractEvent;

/**
 * Created by M2K on 10/04/2015.
 */
public class PlayerDeadEvent extends AbstractEvent {

    private Player deadPlayer;
    private Player killerPlayer;
    private IGameSession game;

    public PlayerDeadEvent(Player deadPlayer, Player killerPlayer, IGameSession game) {
        this.deadPlayer = deadPlayer;
        this.killerPlayer = killerPlayer;
        this.game = game;
    }

    public Player getPlayer() {
        return deadPlayer;
    }

    public Player getKiller() {
        return killerPlayer;
    }

    public IGameSession getGame() {
        return game;
    }
}
