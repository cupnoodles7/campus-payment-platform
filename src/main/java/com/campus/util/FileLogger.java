package com.campus.util;

import org.slf4j.*;;

public class FileLogger {
    private static final Logger logger = LoggerFactory.getLogger(FileLogger.class);

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logError(String message) {
        logger.error(message);
    }

    public static void logDebug(String message) {
        logger.debug(message);
    }

    public static void logWarn(String message) {
        logger.warn(message);
    }

    public static void logTrace(String message) {
        logger.trace(message);
    }
}