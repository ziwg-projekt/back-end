package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.notificator.sms.SMSSender;

import java.time.LocalDateTime;

import static pl.ziwg.backend.template.ReadFileUtils.parseDate;

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

    public void sendAppointmentConfirmation(String number, LocalDateTime vaccinationDate, Address hospitalAddress,
                                            Company vaccineCompany) {
        String message = String.format(
                "Udało się zapisać na szczepienie! Wybrano szczepionkę firmy %s. " +
                "Szczepienie odbędzie się %s w szpitalu pod adresem: ul. %s %s, %s",
                vaccineCompany.getName(),
                parseDate(vaccinationDate),
                hospitalAddress.getStreet(),
                hospitalAddress.getStreetNumber(),
                hospitalAddress.getCity()
        );
        smsSender.sendMessage(number, message);
    }

    public void sendAppointmentReminder(String number, LocalDateTime vaccinationDate) {
        String message = String.format(
                "Przypominamy o umówionej wizycie na szczepienie dnia %s.%n%n Rejestracja szczepień",
                parseDate(vaccinationDate)
        );
        smsSender.sendMessage(number, message);
    }

}
