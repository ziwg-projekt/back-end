package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.notificator.sms.SMSSender;

@Service
public class SMSService {
    private SMSSender smsSender;

    @Autowired
    public SMSService(SMSSender smsSender) {
        this.smsSender = smsSender;
    }

    public void sendVerificationCode(String number, String verificationCode) {
        String message = String.format("Kod weryfikacyjny: %s", verificationCode);
        smsSender.sendMessage(number, message);
    }

    public void sendPasswordReminder(String number, String passwordReminder) {
        String message = String.format("Wpisz dany kod na naszej stronie internetowej w celu zmiany danych: %s",
                passwordReminder);
        smsSender.sendMessage(number, message);
    }

    public void sendVaccinationDate(String number, String vaccinationDate) {
        String message = String.format("Termin szczepienia: %s", vaccinationDate);
        smsSender.sendMessage(number, message);
    }
}
