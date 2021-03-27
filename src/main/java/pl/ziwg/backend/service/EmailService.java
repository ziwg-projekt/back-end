package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.notificator.email.EmailSender;
import pl.ziwg.backend.notificator.email.EmailSubject;

@Service
public class EmailService {
    private EmailSender emailSender;

    @Autowired
    public EmailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendVerificationCode(String mail, String message, EmailSubject subject) {
        emailSender.sendMail(mail, message, subject);
        //TODO: rest of logic, message preparation, maybe change data type from String to something else
    }

    public void sendPasswordReminder(String mail, String message) {
        //TODO: whole logic
    }

}
