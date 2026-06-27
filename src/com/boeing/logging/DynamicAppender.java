package com.boeing.logging;



import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.boeing.logging.LoggerMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicAppender extends AppenderBase<ILoggingEvent> {
    private static final Logger logger = LoggerFactory.getLogger(com.boeing.logging.DynamicAppender.class);
    private final Map<String, Map<Level, RollingFileAppender<ILoggingEvent>>> appenders = new ConcurrentHashMap();
    private Encoder<ILoggingEvent> encoder;
    private String logPath;
    private String maxFileSize = "10MB";
    private String totalSizeCap = "4GB";

    public DynamicAppender() {
    }

    public void start() {
        if (this.logPath == null || this.logPath.isEmpty()) {
            this.logPath = System.getProperty("LOG_PATH", System.getProperty("user.install.root") + "/logs/CustomLogger");
        }

        logger.info("DynamicAppender: Starting with logPath = {}", this.logPath);
        super.start();
    }

    protected void append(ILoggingEvent event) {
        String appName = event.getLoggerName();
        if (appName != null && !appName.isEmpty()) {
            LoggerMonitor.registerLogger(appName);
            LoggerMonitor.trackLogEvent(appName, event.getFormattedMessage().getBytes().length);
            logger.debug("DynamicAppender: append -> appName={}, level={}", appName, event.getLevel());
            Level level = event.getLevel();
            RollingFileAppender<ILoggingEvent> appender = this.getAppenderForLevel(appName, level);
            if (appender != null) {
                appender.doAppend(event);
            } else {
                logger.warn("DynamicAppender: No appender found for appName={} and level={}", appName, level);
            }

        } else {
            logger.warn("DynamicAppender: No application name found; skipping log event.");
        }
    }

    private RollingFileAppender<ILoggingEvent> getAppenderForLevel(String appName, Level level) {
        Map<Level, RollingFileAppender<ILoggingEvent>> levelAppenders = (Map)this.appenders.computeIfAbsent(appName, (k) -> new ConcurrentHashMap());
        RollingFileAppender<ILoggingEvent> fileAppender = (RollingFileAppender)levelAppenders.get(level);
        if (fileAppender == null) {
            fileAppender = this.createAppender(appName, level);
            if (fileAppender != null) {
                levelAppenders.put(level, fileAppender);
            }
        }

        return fileAppender;
    }

    private RollingFileAppender<ILoggingEvent> createAppender(String appName, Level level) {
        try {
            String appLogPath = Paths.get(this.logPath, appName).toString();
            File appFolder = new File(appLogPath);
            if (!appFolder.exists() && !appFolder.mkdirs()) {
                logger.error("DynamicAppender: Failed to create directory {}", appLogPath);
                return null;
            } else {
                LoggerContext context = (LoggerContext)this.getContext();
                RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender();
                fileAppender.setContext(context);
                String logFileName = Paths.get(appLogPath, level.toString().toLowerCase() + ".log").toString();
                fileAppender.setFile(logFileName);
                logger.debug("DynamicAppender: Creating RollingFileAppender for {}", logFileName);
                SizeAndTimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new SizeAndTimeBasedRollingPolicy();
                rollingPolicy.setContext(context);
                rollingPolicy.setParent(fileAppender);
                rollingPolicy.setFileNamePattern(Paths.get(appLogPath, level.toString().toLowerCase() + ".%d{yyyy-MM-dd}.%i.log").toString());
                rollingPolicy.setMaxFileSize(FileSize.valueOf(this.maxFileSize));
                rollingPolicy.setTotalSizeCap(FileSize.valueOf(this.totalSizeCap));
                rollingPolicy.setMaxHistory(90);
                rollingPolicy.start();
                if (this.encoder == null) {
                    logger.error("DynamicAppender: Encoder is not initialized!");
                    return null;
                } else {
                    if (!this.encoder.isStarted()) {
                        this.encoder.start();
                    }

                    fileAppender.setEncoder(this.encoder);
                    fileAppender.setRollingPolicy(rollingPolicy);
                    fileAppender.start();
                    logger.info("DynamicAppender: Successfully created appender for appName={}, level={}", appName, level);
                    return fileAppender;
                }
            }
        } catch (Exception e) {
            logger.error("DynamicAppender: Failed to create appender for appName={}, level={}", new Object[]{appName, level, e});
            return null;
        }
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public void setTotalSizeCap(String totalSizeCap) {
        this.totalSizeCap = totalSizeCap;
    }

    public void removeAppendersForApp(String appName) {
        Map<Level, RollingFileAppender<ILoggingEvent>> levelAppenders = (Map)this.appenders.remove(appName);
        if (levelAppenders != null) {
            levelAppenders.values().forEach((appender) -> {
                appender.stop();
                ((LoggerContext)this.getContext()).getLogger(appName).detachAppender(appender);
                logger.info("DynamicAppender: Removed appender for appName={}, level={}", appName, appender.getName());
            });
        }

    }
}
