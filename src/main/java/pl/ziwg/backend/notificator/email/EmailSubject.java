package pl.ziwg.backend.notificator.email;

import lombok.Getter;

@Getter
public enum EmailSubject {
    VERIFICATION_CODE("Kod weryfikacyjny"),
    PASSWORD_REMINDER("Przypomnienie hasła"),
    VACCINATION_DATE("Termin szczepienia"),
    REGISTRATION_FOR_VACCINATION("Zapis na szczepienie"),
    APPOINTMENT_REMINDER("Przypomnienie o szczepieniu");

    private final String subject;

    EmailSubject(String subject) {
        this.subject = subject;
    }
}
