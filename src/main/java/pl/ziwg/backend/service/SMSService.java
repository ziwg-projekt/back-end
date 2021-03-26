package pl.ziwg.backend.service;

import org.springframework.stereotype.Service;
import pl.ziwg.backend.notificator.sms.SMSSender;

@Service
public class SMSService {
    private SMSSender smsSender = new SMSSender();

    public void sendVerificationCode(String number, String message){
        //TODO: whole logic
    }

    public void sendPasswordReminder(String number, String message){
        //TODO: whole logic
    }
}
