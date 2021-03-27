package pl.ziwg.backend.notificator.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Service
public class EmailSender {
    @Value("{spring.mail.username}")
    private String email;

    private JavaMailSender javaMailSender;

    @Autowired
    public EmailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMail(@Email @NotNull final String destinationEmail,
                         @NotNull final String message,
                         final EmailSubject subject) throws MailException {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(email);
        msg.setTo(destinationEmail);
        msg.setSubject(subject.getSubject());
        msg.setText(message);

        javaMailSender.send(msg);
    }
}
