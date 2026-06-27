package com.boeing.logging;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

class MDCConfig
{
    private static final String APP_NAME_KEY = "applicationName";
    private static final Logger logger = LoggerFactory.getLogger(MDCConfig.class);

    static void setApplicationName(String appName)
    {
        MDC.put("applicationName", appName);
        logger.debug("MDCConfig: Set applicationName to {}", appName);
    }

    static void clear()
    {
        MDC.remove("applicationName");
        logger.debug("MDCConfig: Cleared applicationName from MDC");
    }
}
