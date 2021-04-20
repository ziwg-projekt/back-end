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
    private ReadFileUtils readFileUtils;

    @Autowired
    public EmailService(EmailSender emailSender) {
        this.emailSender = emailSender;
        readFileUtils = new ReadFileUtils();
    }

    public void sendVerificationCode(@Email @NotNull final String mail, @NotNull final String verificationCode,
                                     final EmailSubject subject, @NotNull final String name) {
        final String message = readFileUtils.getVerificationCodeMessage(name, verificationCode);
        emailSender.sendMail(mail, message, subject);
    }

    public void sendPasswordReminder(@Email @NotNull final String mail, @NotNull final String passwordReminder,
                                     final EmailSubject subject, @NotNull final String name) {
        final String message = readFileUtils.getPasswordReminderMessage(name, passwordReminder);
        emailSender.sendMail(mail, message, subject);
    }

    public void sendVaccinationDate(@Email @NotNull final String mail, @NotNull final String vaccinationDate,
                                    final EmailSubject subject, @NotNull final String name) {
        final String message = readFileUtils.getVaccinationDateMessage(name, vaccinationDate);
        emailSender.sendMail(mail, message, subject);
    }

}
