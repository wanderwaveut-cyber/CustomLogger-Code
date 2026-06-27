package com.boeing.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.Layout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

public class EmailAppender
        extends AppenderBase<ILoggingEvent>
{
    private static final Logger logger = LoggerFactory.getLogger(EmailAppender.class);
    private String smtpHost;
    private int smtpPort = 25;
    private String smtpUsername;
    private String smtpPassword;
    private String emailFrom;
    private String emailTo;
    private String emailSubject = "Application Error Notification";
    private PatternLayoutEncoder encoder;

    public void start()
    {
        if ((this.smtpHost == null) || (this.smtpHost.isEmpty()))
        {
            logger.error("EmailAppender: SMTP host is not set. Email feature will be disabled.");
            return;
        }
        if ((this.emailFrom == null) || (this.emailFrom.isEmpty()))
        {
            logger.error("EmailAppender: Email 'from' address is not set. Email feature will be disabled.");
            return;
        }
        if ((this.emailTo == null) || (this.emailTo.isEmpty()))
        {
            logger.error("EmailAppender: Email 'to' address is not set. Email feature will be disabled.");
            return;
        }
        if (this.encoder == null)
        {
            logger.error("EmailAppender: Encoder is not set. Email feature will be disabled.");
            return;
        }
        this.encoder.start();
        super.start();
        logger.info("EmailAppender: Started successfully.");
    }

    protected void append(ILoggingEvent event)
    {
        if (!isStarted()) {
            return;
        }
        Marker emailMarker = LogMarkers.EMAIL;
        if ((event.getMarker() != null) && (event.getMarker().contains(emailMarker)))
        {
            String formattedMessage = this.encoder.getLayout().doLayout(event);
            sendEmail(event.getLevel().toString(), formattedMessage);
        }
    }

    private void sendEmail(String level, String message)
    {
        EmailService emailService = new EmailService(
                this.smtpHost,
                this.smtpPort,
                this.smtpUsername,
                this.smtpPassword);
        try
        {
            emailService.sendEmail(this.emailFrom, this.emailTo, this.emailSubject + " - " + level, message);
            logger.debug("EmailAppender: Sent email for log level {}", level);
        }
        catch (Exception e)
        {
            logger.error("EmailAppender: Failed to send email for log level {}", level, e);
        }
    }

    public void setSmtpHost(String smtpHost)
    {
        this.smtpHost = smtpHost;
    }

    public void setSmtpPort(int smtpPort)
    {
        this.smtpPort = smtpPort;
    }

    public void setSmtpUsername(String smtpUsername)
    {
        this.smtpUsername = smtpUsername;
    }

    public void setSmtpPassword(String smtpPassword)
    {
        this.smtpPassword = smtpPassword;
    }

    public void setEmailFrom(String emailFrom)
    {
        this.emailFrom = emailFrom;
    }

    public void setEmailTo(String emailTo)
    {
        this.emailTo = emailTo;
    }

    public void setEmailSubject(String emailSubject)
    {
        this.emailSubject = emailSubject;
    }

    public void setEncoder(PatternLayoutEncoder encoder)
    {
        this.encoder = encoder;
    }
}
