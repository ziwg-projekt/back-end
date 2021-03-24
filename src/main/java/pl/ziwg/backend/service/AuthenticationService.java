package pl.ziwg.backend.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.ziwg.backend.exception.*;
import pl.ziwg.backend.notificator.NotificationType;
import pl.ziwg.backend.security.RegistrationCode;
import pl.ziwg.backend.security.VerificationEntry;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthenticationService {
    protected static final Logger log = Logger.getLogger(AuthenticationService.class);

    private Map<String, VerificationEntry> verificationEntryList = new HashMap<>();

    public String generateRegistrationCode(Map<String, Object> registrationDetails){
        String pesel;
        NotificationType notificationType;
        if(!registrationDetails.containsKey("pesel") || !registrationDetails.containsKey("notification_type")){
            throw new InvalidRequestException("Request body should contain JSON with 'pesel' and 'notification_type' keys");
        }

        try {
            pesel = (String) registrationDetails.get("pesel");
            notificationType = NotificationType.values()[(int) registrationDetails.get("notification_type")];
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e){
            throw new InvalidRequestException("Field 'pesel' should be String and field 'notification_type' should be 0 (for SMS verification) or 1 (for e-mail verification)");
        }

        log.info("New registration request, PESEL: '" + pesel + "' and notification type: '" + notificationType.toString() + "'");
        VerificationEntry verificationEntry = new VerificationEntry(createRegistrationCode(), RandomStringUtils.randomAlphanumeric(30));
        verificationEntryList.put(pesel, verificationEntry);
        checkIfPeselExists(pesel);
        //TODO: whole logic, add notification and token generation, check if pesel exists etc.
        return verificationEntry.getToken();
    }

    public Map<String, String> verifyRegistrationCode(String pesel, String code, String token){
        checkIfRegistrationCodeForPeselExists(pesel);
        RegistrationCode registrationCode = verificationEntryList.get(pesel).getRegistrationCode();

        if(verificationEntryList.get(pesel).getToken().equals(token)){
            checkIfCodeIsCorrect(registrationCode, code);
            verificationEntryList.remove(pesel);
            log.info("Registration succeeded: PESEL: '" + pesel);
            return Map.of(
                    "name", "Mikołaj",
                    "surname", "Kamiński"
            );
        }
        else{
            log.error("PeselNotCorrespondingToTokenException: PESEL: '" + pesel + "', token: " + token + "'");
            throw new PeselNotCorrespondingToTokenException("Token is not corresponding to pesel");
        }
    }

    private void checkIfCodeIsCorrect(RegistrationCode registrationCode, String code){
        if(registrationCode.getCode().equals(code)){
            checkIfRegistrationCodeExpired(registrationCode);
        }
        else{
            log.error("IncorrectRegistrationCodeException: registrationCode: '" + registrationCode + "', code: " + code + "'");
            throw new IncorrectRegistrationCodeException("Registration code is incorrect");
        }
    }

    private void checkIfRegistrationCodeExpired(RegistrationCode registrationCode){
        if(registrationCode.isExpire()){
            log.error("RegistrationCodeExpiredException: " + registrationCode.getHowManyCodeExistsInSeconds() + "s > " + registrationCode.getExpireIn() + "s");
            throw new RegistrationCodeExpiredException("Token expired, " + registrationCode.getHowManyCodeExistsInSeconds() + "s > " + registrationCode.getExpireIn() + "s");
        }
    }
    private void checkIfRegistrationCodeForPeselExists(String pesel){
        if(verificationEntryList.get(pesel)==null){
            log.error("NoCodeForGivenPeselException: PESEL: '" + pesel);
            throw new NoCodeForGivenPeselException("Pesel doesn't exist in cache, validate it");
        }
    }

    private void checkIfPeselExists(String pesel){
        //TODO: check in remote API
        if(false){
            log.error("PeselDoesNotExistsException: PESEL: '" + pesel);
            throw new PeselDoesNotExistsException("Pesel does not exists!");
        }
    }

    private RegistrationCode createRegistrationCode() {
        String code = RandomStringUtils.randomNumeric(6);
        return new RegistrationCode(code, 60);
    }
}
