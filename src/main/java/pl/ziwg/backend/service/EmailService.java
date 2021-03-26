package pl.ziwg.backend.service;

import org.springframework.stereotype.Service;
import pl.ziwg.backend.notificator.email.EmailSender;

@Service
public class EmailService {
    private EmailSender emailSender = new EmailSender();

    public void sendVerificationCode(String mail, String message){
        emailSender.sendMail(mail, message);
        //TODO: rest of logic, message preparation, maybe change data type from String to something else
    }

    public void sendPasswordReminder(String mail, String message){
        //TODO: whole logic
    }

}
