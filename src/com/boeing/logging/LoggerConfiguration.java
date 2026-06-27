package com.boeing.logging;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import java.lang.management.ManagementFactory;
import java.util.Optional;

import com.boeing.logging.DynamicAppender;
import com.boeing.logging.EmailAppender;
import com.boeing.logging.LoggerOptions;
import com.boeing.logging.MDCConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LoggerConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(com.boeing.logging.LoggerConfiguration.class);
    private static final String DEFAULT_LOG_PATH = System.getProperty("user.install.root") + "/logs/CustomLogger";
    private static final String DEFAULT_PATTERN = "[%d{M/d/yy HH:mm:ss:SSS z}] " + ManagementFactory.getRuntimeMXBean().getName() + " %thread %-5level %logger{36} - %msg%n";

    LoggerConfiguration() {
    }

    static void configureBase() {
        logger.info("Configuring base logger with default settings.");
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
        context.reset();
        String logPath = System.getProperty("LOG_PATH", DEFAULT_LOG_PATH);
        PatternLayoutEncoder encoder = createEncoder(context, DEFAULT_PATTERN);
        com.boeing.logging.DynamicAppender dynamicAppender = createDynamicAppender(context, encoder, logPath, "10MB", "4GB");
        context.getLogger("ROOT").addAppender(dynamicAppender);
        logger.info("Base logger configured with logPath={}", logPath);
    }

    static void configureBase(com.boeing.logging.LoggerOptions options) {
        logger.info("Configuring base logger with options: {}", options);
        LoggerContext context = (LoggerContext)LoggerFactory.getILoggerFactory();
        context.reset();
        String logPath = (String)Optional.ofNullable(options.getLogPath()).orElse(System.getProperty("LOG_PATH", DEFAULT_LOG_PATH));
        String patternToUse = (String)Optional.ofNullable(options.getLogPattern()).filter((p) -> !p.isEmpty()).orElse(DEFAULT_PATTERN);
        String maxFileSize = (String)Optional.ofNullable(options.getMaxFileSize()).orElse("10MB");
        String totalSizeCap = (String)Optional.ofNullable(options.getTotalSizeCap()).orElse("4GB");
        PatternLayoutEncoder encoder = createEncoder(context, patternToUse);
        com.boeing.logging.DynamicAppender dynamicAppender = createDynamicAppender(context, encoder, logPath, maxFileSize, totalSizeCap);
        context.getLogger("ROOT").addAppender(dynamicAppender);
        if (options.isEmailEnabled()) {
            com.boeing.logging.EmailAppender emailAppender = createEmailAppender(context, options);
            if (emailAppender != null) {
                context.getLogger("ROOT").addAppender(emailAppender);
                logger.info("EmailAppender configured and added for application={}", options.getAppName());
            } else {
                logger.warn("EmailAppender was not configured due to missing configurations.");
            }
        }

        logger.info("Base logger configured with logPath={}, maxFileSize={}, totalSizeCap={}", new Object[]{logPath, maxFileSize, totalSizeCap});
    }

    static void configureForApp(String appName) {
        com.boeing.logging.MDCConfig.setApplicationName(appName);
        logger.debug("Configured MDC for application: {}", appName);
    }

    private static PatternLayoutEncoder createEncoder(LoggerContext context, String pattern) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(pattern);
        encoder.start();
        logger.debug("PatternLayoutEncoder created with pattern: {}", pattern);
        return encoder;
    }

    private static com.boeing.logging.DynamicAppender createDynamicAppender(LoggerContext context, PatternLayoutEncoder encoder, String logPath, String maxFileSize, String totalSizeCap) {
        com.boeing.logging.DynamicAppender dynamicAppender = new DynamicAppender();
        dynamicAppender.setContext(context);
        dynamicAppender.setEncoder(encoder);
        dynamicAppender.setLogPath(logPath);
        dynamicAppender.setMaxFileSize(maxFileSize);
        dynamicAppender.setTotalSizeCap(totalSizeCap);
        dynamicAppender.start();
        logger.debug("DynamicAppender created and started with logPath={}, maxFileSize={}, totalSizeCap={}", new Object[]{logPath, maxFileSize, totalSizeCap});
        return dynamicAppender;
    }

    private static com.boeing.logging.EmailAppender createEmailAppender(LoggerContext context, LoggerOptions options) {
        try {
            PatternLayoutEncoder emailEncoder = new PatternLayoutEncoder();
            emailEncoder.setContext(context);
            emailEncoder.setPattern("[%d{yyyy-MM-dd HH:mm:ss}] %-5level %logger{36} - %msg%n");
            emailEncoder.start();
            com.boeing.logging.EmailAppender emailAppender = new EmailAppender();
            emailAppender.setContext(context);
            emailAppender.setEncoder(emailEncoder);
            emailAppender.setSmtpHost(options.getSmtpHost());
            emailAppender.setSmtpPort(options.getSmtpPort());
            emailAppender.setSmtpUsername(options.getSmtpUsername());
            emailAppender.setSmtpPassword(options.getSmtpPassword());
            emailAppender.setEmailFrom(options.getEmailFrom());
            emailAppender.setEmailTo(options.getEmailTo());
            emailAppender.setEmailSubject(options.getEmailSubject());
            emailAppender.start();
            logger.debug("EmailAppender created with SMTP host={}, port={}", options.getSmtpHost(), options.getSmtpPort());
            return emailAppender;
        } catch (Exception e) {
            logger.error("Failed to create EmailAppender due to missing or invalid configurations.", e);
            return null;
        }
    }
}
