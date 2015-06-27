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
package it.kytech.bowwarfare.utils;

import com.google.common.base.Optional;
import org.slf4j.Logger;

/**
 * Created by M2K on 10/04/2015.
 */
public class LogHelper {

    public static LogHelper instance;
    private Logger logger;
    private boolean isDebug;

    private LogHelper(Logger logger, boolean isDebug) {
        this.logger = logger;
        this.isDebug = isDebug;
    }

    public static LogHelper setup(Logger logger, boolean debug) throws IllegalStateException {
        if (instance != null) {
            throw new IllegalStateException("This class has already been initialized!");
        }
        instance = new LogHelper(logger, debug);
        return instance;
    }

    public static Optional<LogHelper> getInstance() {
        return (instance == null) ? Optional.<LogHelper>absent() : Optional.of(instance);
    }

    public Logger getLogger() {
        return logger;
    }

    public void log(String msg) {
        logger.info(msg);
    }

    public void logError(String msg) {
        logger.error(msg);
    }

    public void logWarn(String msg) {
        logger.warn(msg);
    }

    public void debug(String msg) {
        if (isDebug) {
            logger.debug("DEBUG: " + msg);
        }
    }

    public void setDebug(boolean value) {
        isDebug = value;
        logger.info("Changed, DEBUG Mode: " + isDebug);
    }
}
