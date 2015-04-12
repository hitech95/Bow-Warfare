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
package it.kytech.bowwarfare.utils;

import com.google.inject.Inject;
import org.slf4j.Logger;

/**
 * Created by M2K on 10/04/2015.
 */
public class LogHelper {

    public static Logger logger;

    private static LogHelper instance;

    public static LogHelper getInstance() {
        if (instance == null) {
            instance = new LogHelper();
        }

        return instance;
    }

    public org.slf4j.Logger getLogger() {
        return logger;
    }

    @Inject
    private void setLogger(org.slf4j.Logger logger) {
        logger = logger;
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
        if (ConfigurationHelper.getInstance().isDebug()) {
            logger.debug(msg);
        }
    }
}
