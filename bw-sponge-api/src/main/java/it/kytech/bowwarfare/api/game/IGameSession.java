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
package it.kytech.bowwarfare.api.game;

import it.kytech.bowwarfare.api.score.IScore;
import org.spongepowered.api.entity.player.Player;

/**
 * Created by M2K on 10/04/2015.
 */
public interface IGameSession {

    IGameSession getSnapshot();

    IArena getArena();

    IGameMode getGameMode();

    Player[] getPlayers();

    int getPlayerCount();

    Player[] getPlayers(ITeam team);

    ITeam[] getTeams();

    int getTeamCount();

    ITeam getTeamBySlug(String slug);

    ITeam getTeamByPlayer(Player player);

    IScore getScore();

    IScore getScore(Player player);

    IScore getScore(ITeam team);

}
