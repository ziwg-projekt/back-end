package pl.ziwg.backend.notificator.email;

import lombok.Getter;

@Getter
public enum EmailSubject {
    VERIFICATION_CODE("Kod weryfikacyjny"),
    PASSWORD_REMINDER("Przypomnienie hasła"),
    VACCINATION_DATE("Termin szczepienia");

    private final String subject;

    EmailSubject(String subject) {
        this.subject = subject;
    }
}
