package com.boeing.logging.test;

import com.boeing.logging.ApplicationLoggerFactory;
import com.boeing.logging.ExtendedLogger;

public class LoggerTest
{
    public static void main(String[] args)
    {
        ExtendedLogger fileLogger = ApplicationLoggerFactory.getLogger(
                "TEST",
                "C:/tmp/NEW_LOGS",
                null,
                null,
                null,
                false,
                null,
                0,
                null,
                null,
                null,
                null,
                null);

        fileLogger.info("Application started without email notifications.");
        fileLogger.debug("Debugging application without email.");
        fileLogger.error("An error occurred without email notification.");


    }
}
