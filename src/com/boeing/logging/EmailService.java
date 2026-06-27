package com.boeing.logging;


import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService
{
    private final String smtpHost;
    private final int smtpPort;
    private final String smtpUsername;
    private final String smtpPassword;

    public EmailService(String smtpHost, int smtpPort, String smtpUsername, String smtpPassword)
    {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.smtpUsername = smtpUsername;
        this.smtpPassword = smtpPassword;
    }

    public void sendEmail(String from, String to, String subject, String body)
            throws MessagingException
    {
        Properties props = new Properties();
        props.put("mail.smtp.auth", Boolean.valueOf((this.smtpUsername != null) && (!this.smtpUsername.isEmpty())));
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", this.smtpHost);
        props.put("mail.smtp.port", String.valueOf(this.smtpPort));
        Session session;

        if ((this.smtpUsername != null) && (!this.smtpUsername.isEmpty()))
        {
            Authenticator auth = new Authenticator()
            {
                protected PasswordAuthentication getPasswordAuthentication()
                {
                    return new PasswordAuthentication(EmailService.this.smtpUsername, EmailService.this.smtpPassword);
                }
            };
            session = Session.getInstance(props, auth);
        }
        else
        {
            session = Session.getInstance(props);
        }
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(
                Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        message.setText(body);

        Transport.send(message);
    }
}
