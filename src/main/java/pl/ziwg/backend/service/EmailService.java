package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.notificator.email.EmailSender;
import pl.ziwg.backend.notificator.email.EmailSubject;
import pl.ziwg.backend.template.ReadFileUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Service
public class EmailService {
    private EmailSender emailSender;

    @Autowired
    public EmailService(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendVerificationCode(@Email @NotNull final String mail, @NotNull final String verificationCode,
                                     final EmailSubject subject, @NotNull final String name) {
        final String message = ReadFileUtils.getVerificationCodeMessage(name, verificationCode);
        emailSender.sendMail(mail, message, subject);
    }

    public void sendPasswordReminder(String mail, String message) {
        //TODO: whole logic
    }

}
