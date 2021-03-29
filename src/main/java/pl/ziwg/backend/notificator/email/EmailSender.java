package pl.ziwg.backend.notificator.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Service
public class EmailSender {
    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    @Value("{spring.mail.username}")
    private String email;

    private JavaMailSender javaMailSender;

    @Autowired
    public EmailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Async
    public void sendMail(@Email @NotNull final String destinationEmail,
                         @NotNull final String message, final EmailSubject subject) {
        final SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(email);
        msg.setTo(destinationEmail);
        msg.setSubject(subject.getSubject());
        msg.setText(message);
        try {
            javaMailSender.send(msg);
            logger.info("Mail sent successfully!");
        } catch (MailException e) {
            logger.error(String.format("Unable to send email trace %s", e));
        }
    }
}
