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
package it.kytech.bowwarfare.game.manager;

import it.kytech.bowwarfare.api.game.IArena;
import it.kytech.bowwarfare.api.game.IArenaManager;
import it.kytech.bowwarfare.configuration.ArenaConfiguration;
import it.kytech.bowwarfare.game.Arena;
import org.spongepowered.api.Game;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hitech95 on 27/06/2015.
 */
public class ArenaManager implements IArenaManager {

    ArenaConfiguration config;
    ArrayList<IArena> arenas;

    public ArenaManager(Game game, ArenaConfiguration config) {
        this.config = config;
        arenas = new ArrayList<IArena>();

        for (String slug : config.listArenas()) {
            arenas.add(new Arena(game.getServer(), config.loadArena(slug)));
        }
    }

    @Override
    public List<IArena> listArenas() {
        return null;
    }

    @Override
    public List<IArena> listArenas(boolean onlyEnabled) {
        return null;
    }

    @Override
    public IArena getArena(String slug) {
        return null;
    }

    @Override
    public boolean createArena(IArena arena) {
        return false;
    }
}
