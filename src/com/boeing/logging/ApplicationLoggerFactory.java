package com.boeing.logging;

import ch.qos.logback.classic.LoggerContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;

public class ApplicationLoggerFactory
{
    private static final org.slf4j.Logger factoryLogger = LoggerFactory.getLogger(ApplicationLoggerFactory.class);
    private static final Map<String, LoggerOptions> APP_LOGGER_CACHE = new ConcurrentHashMap();

    public static ExtendedLogger getLogger(String appName)
    {
        MDCConfig.setApplicationName(appName);
        return getLogger(new LoggerOptions.Builder(appName).build());
    }

    public static ExtendedLogger getLogger(String appName, String logPath, String maxFileSize, String totalSizeCap, String logPattern, boolean emailEnabled, String smtpHost, int smtpPort, String smtpUsername, String smtpPassword, String emailFrom, String emailTo, String emailSubject)
    {
        LoggerOptions.Builder builder = new LoggerOptions.Builder(appName)
                .logPath(logPath)
                .maxFileSize(maxFileSize)
                .totalSizeCap(totalSizeCap)
                .logPattern(logPattern)
                .enableEmail(emailEnabled);
        if (emailEnabled) {
            builder.smtpHost(smtpHost).smtpPort(smtpPort).smtpUsername(smtpUsername).smtpPassword(smtpPassword).emailFrom(emailFrom).emailTo(emailTo).emailSubject(emailSubject);
        }
        LoggerOptions options = builder.build();
        MDCConfig.setApplicationName(appName);
        return getLogger(options);
    }

    public static ExtendedLogger getLogger(LoggerOptions options)
    {
        String appName = options.getAppName();
        if ((appName == null) || (appName.trim().isEmpty())) {
            throw new IllegalArgumentException("Application name cannot be null or empty");
        }
        LoggerOptions existingOptions = (LoggerOptions)APP_LOGGER_CACHE.get(appName);
        if ((existingOptions != null) && (!isDifferentConfig(existingOptions, options)))
        {
            factoryLogger.debug("Using cached logger for app={}", appName);
            org.slf4j.Logger existingLogger = LoggerFactory.getLogger(appName);
            return new ExtendedLogger(existingLogger);
        }
        synchronized (ApplicationLoggerFactory.class)
        {
            existingOptions = (LoggerOptions)APP_LOGGER_CACHE.get(appName);
            if ((existingOptions != null) && (!isDifferentConfig(existingOptions, options)))
            {
                factoryLogger.debug("Using cached logger for app={}", appName);
                org.slf4j.Logger existingLogger = LoggerFactory.getLogger(appName);
                return new ExtendedLogger(existingLogger);
            }
            factoryLogger.info("Configuring new logger for app={}", appName);
            try
            {
                LoggerConfiguration.configureBase(options);
                LoggerConfiguration.configureForApp(appName);
                APP_LOGGER_CACHE.put(appName, options);
                factoryLogger.info("Logger configured successfully for app={}", appName);
            }
            catch (Exception e)
            {
                factoryLogger.error("Failed to configure logger for app={}", appName, e);
                throw new RuntimeException("Failed to configure logger for app: " + appName, e);
            }
            org.slf4j.Logger newLogger = LoggerFactory.getLogger(appName);
            return new ExtendedLogger(newLogger);
        }
    }

    public static void reloadLogger(String appName, String logPath, String maxFileSize, String totalSizeCap, String logPattern, boolean emailEnabled, String smtpHost, int smtpPort, String smtpUsername, String smtpPassword, String emailFrom, String emailTo, String emailSubject)
    {
        factoryLogger.info("Reloading logger for application: {}", appName);
        factoryLogger.info("New configuration - LogPath: {}, MaxFileSize: {}, TotalSizeCap: {}, LogPattern: {}, EmailEnabled: {}", new Object[] {
                logPath, maxFileSize, totalSizeCap, logPattern, Boolean.valueOf(emailEnabled) });
        try
        {
            LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
            context.getLogger(appName).detachAndStopAllAppenders();

            LoggerOptions.Builder builder = new LoggerOptions.Builder(appName)
                    .logPath(logPath)
                    .maxFileSize(maxFileSize)
                    .totalSizeCap(totalSizeCap)
                    .logPattern(logPattern)
                    .enableEmail(emailEnabled);
            if (emailEnabled) {
                builder.smtpHost(smtpHost).smtpPort(smtpPort).smtpUsername(smtpUsername).smtpPassword(smtpPassword).emailFrom(emailFrom).emailTo(emailTo).emailSubject(emailSubject);
            }
            LoggerOptions newOptions = builder.build();

            getLogger(newOptions);

            factoryLogger.info("Logger for application {} successfully reloaded.", appName);
        }
        catch (Exception e)
        {
            factoryLogger.error("Error reloading logger for application {}", appName, e);
            throw new RuntimeException("Error reloading logger for application: " + appName, e);
        }
    }

    private static boolean isDifferentConfig(LoggerOptions oldOpts, LoggerOptions newOpts)
    {
        if (oldOpts == null) {
            return true;
        }
        return (!safeEq(oldOpts.getLogPath(), newOpts.getLogPath())) ||
                (!safeEq(oldOpts.getMaxFileSize(), newOpts.getMaxFileSize())) ||
                (!safeEq(oldOpts.getTotalSizeCap(), newOpts.getTotalSizeCap())) ||
                (!safeEq(oldOpts.getLogPattern(), newOpts.getLogPattern())) ||
                (oldOpts.isEmailEnabled() != newOpts.isEmailEnabled()) ||
                (!safeEq(oldOpts.getSmtpHost(), newOpts.getSmtpHost())) ||
                (oldOpts.getSmtpPort() != newOpts.getSmtpPort()) ||
                (!safeEq(oldOpts.getSmtpUsername(), newOpts.getSmtpUsername())) ||
                (!safeEq(oldOpts.getSmtpPassword(), newOpts.getSmtpPassword())) ||
                (!safeEq(oldOpts.getEmailFrom(), newOpts.getEmailFrom())) ||
                (!safeEq(oldOpts.getEmailTo(), newOpts.getEmailTo())) ||
                (!safeEq(oldOpts.getEmailSubject(), newOpts.getEmailSubject()));
    }

    private static boolean safeEq(String s1, String s2)
    {
        if (s1 == null) {
            s1 = "";
        }
        if (s2 == null) {
            s2 = "";
        }
        return s1.equals(s2);
    }
}
