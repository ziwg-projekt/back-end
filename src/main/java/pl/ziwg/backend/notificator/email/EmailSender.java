package pl.ziwg.backend.notificator.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Properties;

@Service
public class EmailSender {
    protected static final Logger log = LoggerFactory.getLogger(EmailSender.class);
    @Value("${spring.mail.username}")
    private String email;

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private String port;

    @Value("${spring.mail.password}")
    private String password;

    @Async
    public void sendMail(@Email @NotNull final String destinationEmail,
                         @NotNull final String message, final EmailSubject subject) {
        Properties properties = getProperties();
        Authenticator auth = new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        };
        Session session = Session.getInstance(properties, auth);
        Message msg = new MimeMessage(session);
        try {
            msg.setFrom(new InternetAddress(email));
            final InternetAddress to = new InternetAddress(destinationEmail);
            msg.setRecipient(Message.RecipientType.TO, to);
            msg.setSubject(subject.getSubject());
            msg.setSentDate(new Date());
            msg.setContent(message, "text/html; charset=utf-8");
            Transport.send(msg);
            log.info("Mail sent successfully!");
        } catch (AddressException ae) {
            log.info("Email address not found");
            ae.printStackTrace();
        } catch (MessagingException me) {
            log.info("The message could not be sent ");
            me.printStackTrace();
        }
    }

    private Properties getProperties() {
        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        return properties;
    }
}
