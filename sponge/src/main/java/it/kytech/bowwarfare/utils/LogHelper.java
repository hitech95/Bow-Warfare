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
