package pl.ziwg.backend.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificationEntry {
    private RegistrationCode registrationCode;
    private String token;

    public VerificationEntry(RegistrationCode registrationCode, String token) {
        this.registrationCode = registrationCode;
        this.token = token;
    }
}
