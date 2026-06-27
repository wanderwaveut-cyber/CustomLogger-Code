package com.boeing.logging;

public class LoggerOptions
{
    private final String appName;
    private final String logPath;
    private final String maxFileSize;
    private final String totalSizeCap;
    private final String logPattern;
    private final boolean emailEnabled;
    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;
    private final String emailFrom;
    private final String emailTo;
    private final String emailSubject;

    private LoggerOptions(Builder builder, Object o)
    {
        this.appName = builder.appName;
        this.logPath = builder.logPath;
        this.maxFileSize = builder.maxFileSize;
        this.totalSizeCap = builder.totalSizeCap;
        this.logPattern = builder.logPattern;
        this.emailEnabled = builder.emailEnabled;
        this.smtpHost = builder.smtpHost;
        this.smtpPort = builder.smtpPort;
        this.smtpUsername = builder.smtpUsername;
        this.smtpPassword = builder.smtpPassword;
        this.emailFrom = builder.emailFrom;
        this.emailTo = builder.emailTo;
        this.emailSubject = builder.emailSubject;
    }

    public String getAppName()
    {
        return this.appName;
    }

    public String getLogPath()
    {
        return this.logPath;
    }

    public String getMaxFileSize()
    {
        return this.maxFileSize;
    }

    public String getTotalSizeCap()
    {
        return this.totalSizeCap;
    }

    public String getLogPattern()
    {
        return this.logPattern;
    }

    public boolean isEmailEnabled()
    {
        return this.emailEnabled;
    }

    public String getSmtpHost()
    {
        return this.smtpHost;
    }

    public int getSmtpPort()
    {
        return this.smtpPort;
    }

    public String getSmtpUsername()
    {
        return this.smtpUsername;
    }

    public String getSmtpPassword()
    {
        return this.smtpPassword;
    }

    public String getEmailFrom()
    {
        return this.emailFrom;
    }

    public String getEmailTo()
    {
        return this.emailTo;
    }

    public String getEmailSubject()
    {
        return this.emailSubject;
    }

    public String toString()
    {
        return

                "LoggerOptions{appName='" + this.appName + '\'' + ", logPath='" + this.logPath + '\'' + ", maxFileSize='" + this.maxFileSize + '\'' + ", totalSizeCap='" + this.totalSizeCap + '\'' + ", logPattern='" + this.logPattern + '\'' + ", emailEnabled=" + this.emailEnabled + ", smtpHost='" + this.smtpHost + '\'' + ", smtpPort=" + this.smtpPort + ", smtpUsername='" + this.smtpUsername + '\'' + ", smtpPassword='******'" + ", emailFrom='" + this.emailFrom + '\'' + ", emailTo='" + this.emailTo + '\'' + ", emailSubject='" + this.emailSubject + '\'' + '}';
    }

    public static class Builder
    {
        private final String appName;
        private String logPath;
        private String maxFileSize = "10MB";
        private String totalSizeCap = "4GB";
        private String logPattern;
        private boolean emailEnabled = false;
        private String smtpHost;
        private int smtpPort = 25;
        private String smtpUsername;
        private String smtpPassword;
        private String emailFrom;
        private String emailTo;
        private String emailSubject = "Application Error Notification";

        public Builder(String appName)
        {
            if ((appName == null) || (appName.trim().isEmpty())) {
                throw new IllegalArgumentException("Application name cannot be null or empty");
            }
            this.appName = appName;
        }

        public Builder logPath(String logPath)
        {
            this.logPath = logPath;
            return this;
        }

        public Builder maxFileSize(String maxFileSize)
        {
            this.maxFileSize = maxFileSize;
            return this;
        }

        public Builder totalSizeCap(String totalSizeCap)
        {
            this.totalSizeCap = totalSizeCap;
            return this;
        }

        public Builder logPattern(String logPattern)
        {
            this.logPattern = logPattern;
            return this;
        }

        public Builder enableEmail(boolean enabled)
        {
            this.emailEnabled = enabled;
            return this;
        }

        public Builder smtpHost(String smtpHost)
        {
            this.smtpHost = smtpHost;
            return this;
        }

        public Builder smtpPort(int smtpPort)
        {
            this.smtpPort = smtpPort;
            return this;
        }

        public Builder smtpUsername(String smtpUsername)
        {
            this.smtpUsername = smtpUsername;
            return this;
        }

        public Builder smtpPassword(String smtpPassword)
        {
            this.smtpPassword = smtpPassword;
            return this;
        }

        public Builder emailFrom(String emailFrom)
        {
            this.emailFrom = emailFrom;
            return this;
        }

        public Builder emailTo(String emailTo)
        {
            this.emailTo = emailTo;
            return this;
        }

        public Builder emailSubject(String emailSubject)
        {
            this.emailSubject = emailSubject;
            return this;
        }

        public LoggerOptions build()
        {
            if (this.emailEnabled)
            {
                if ((this.smtpHost == null) || (this.smtpHost.trim().isEmpty())) {
                    throw new IllegalArgumentException("SMTP host must be provided when email is enabled");
                }
                if ((this.emailFrom == null) || (this.emailFrom.trim().isEmpty())) {
                    throw new IllegalArgumentException("Email 'from' address must be provided when email is enabled");
                }
                if ((this.emailTo == null) || (this.emailTo.trim().isEmpty())) {
                    throw new IllegalArgumentException("Email 'to' address must be provided when email is enabled");
                }
            }
            return new com.boeing.logging.LoggerOptions(this, (com.boeing.logging.LoggerOptions)null);
        }
    }
}
