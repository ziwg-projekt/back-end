package pl.ziwg.backend.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VerificationEntry {
    private RegistrationCode registrationCode;
    private String verificationToken;
    private String registrationToken;
    private boolean verified;

    public VerificationEntry(RegistrationCode registrationCode, String verificationToken) {
        this.registrationCode = registrationCode;
        this.verificationToken = verificationToken;
        this.verified = false;
    }
}
