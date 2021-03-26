package pl.ziwg.backend.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.exception.*;
import pl.ziwg.backend.notificator.NotificationType;
import pl.ziwg.backend.security.RegistrationCode;
import pl.ziwg.backend.security.VerificationEntry;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthenticationService {
    protected static final Logger log = Logger.getLogger(AuthenticationService.class);

    private Map<String, VerificationEntry> verificationEntryList = new HashMap<>();

    public void checkIfCorrectGenerationCodeRequestBody(Map<String, Object> registrationDetails){
        if(!registrationDetails.containsKey("pesel") || !registrationDetails.containsKey("verification_type")){
            throw new InvalidRequestException("Request body should contain JSON with 'pesel' and 'notification_type' keys");
        }
    }

    public void checkIfCorrectRegistrationCodeRequestBody(Map<String, String> verificationDetails){
        if(!verificationDetails.containsKey("registration_code")){
            throw new InvalidRequestException("Request body should contain JSON with 'registration_code' key");
        }
    }

    public Map<String, String> sendVerificationCodeToUser(Map<String, Object> registrationDetails){
        Map.Entry<String, VerificationEntry> entry = getVerificationEntryBaseOnPesel(registrationDetails);
        //TODO: notify person with pesel
        validateIfRegistrationIsPossible(entry.getKey());
        return getVerificationApiPath(entry.getValue());
    }

    public Map<String, String> verifyRegistrationCodeCorrectness(Map<String, String> verificationDetails, String verificationToken){
        Map.Entry<String, VerificationEntry> entry = getMapEntryByVerificationToken(verificationToken);
        validateIfVerificationSuccessful(entry, verificationDetails.get("registration_code"));
        return allowRegistration(entry.getKey());
    }

    public void registerUser(String registrationToken, Map<String, Object> userData){
        Map.Entry<String, VerificationEntry> entry = getMapEntryByRegistrationToken(registrationToken);
        //TODO: make registration
        verificationEntryList.remove(entry.getKey());
    }

    private Map<String, String> getVerificationApiPath(VerificationEntry verificationEntry){
        return Map.of(
                "verify_api_path", "/api/v1/auth/registration/code/verify/" + verificationEntry.getVerificationToken(),
                "registration_code", verificationEntry.getRegistrationCode().getCode()  // to be deleted when notification system will be done
        );
    }

    private void validateIfRegistrationIsPossible(String pesel){
        checkIfPeselExists(pesel);
        checkIfAlreadyRegistered(pesel);
    }

    private void validateIfVerificationSuccessful(Map.Entry<String, VerificationEntry> entry, String registrationCode){
        checkIfRegistrationAlreadyVerified(entry);
        checkIfRegistrationCodeIsCorrect(entry.getValue().getRegistrationCode(), registrationCode);
        checkIfRegistrationCodeExpired(entry.getValue().getRegistrationCode());
    }

    private void checkIfRegistrationAlreadyVerified(Map.Entry<String, VerificationEntry> entry){
        if(entry.getValue().isVerified()){
            throw new VerificationAlreadySucceededException("That verification was done before and there is no possibility to generate registration code one more time");
        }
    }

    private Map.Entry<String, VerificationEntry> getVerificationEntryBaseOnPesel(Map<String, Object> registrationDetails){
        try {
            String pesel = (String) registrationDetails.get("pesel");
            NotificationType notificationType = NotificationType.values()[(int) registrationDetails.get("verification_type")];
            log.info("New registration request, PESEL: '" + pesel + "' and verification type: '" + notificationType.toString() + "'");
            return generateVerificationEntry(pesel);
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e){
            throw new InvalidRequestException("Field 'pesel' should be String and field 'verification_type' should be 0 (for SMS verification) or 1 (for e-mail verification)");
        }
    }

    private Map.Entry<String, VerificationEntry> generateVerificationEntry(String pesel){
        VerificationEntry verificationEntry = new VerificationEntry(createRegistrationCode(), RandomStringUtils.randomAlphanumeric(30));
        verificationEntryList.put(pesel, verificationEntry);
        return getMapEntryByVerificationToken(verificationEntry.getVerificationToken());
    }

    private Map.Entry<String, VerificationEntry> getMapEntryByVerificationToken(String token){
        for (Map.Entry<String,VerificationEntry> entry : verificationEntryList.entrySet()){
            if(entry.getValue().getVerificationToken().equals(token)){
                return entry;
            }
        }
        log.error("TokenDoesNotExistsException: verification token: " + token + "'");
        throw new TokenDoesNotExistsException("There is no such a verification token");
    }

    private Map.Entry<String, VerificationEntry> getMapEntryByRegistrationToken(String token){
        for (Map.Entry<String,VerificationEntry> entry : verificationEntryList.entrySet()){
            if(entry.getValue().getRegistrationToken().equals(token)){
                return entry;
            }
        }
        log.error("TokenDoesNotExistsException: registration token: " + token + "'");
        throw new TokenDoesNotExistsException("There is no such a registration token");
    }

    private Map<String, String> allowRegistration(String pesel){
        verificationEntryList.get(pesel).setVerified(true);
        verificationEntryList.get(pesel).setRegistrationToken(RandomStringUtils.randomAlphanumeric(30));
        log.info("Registration succeeded: PESEL: '" + pesel);
        return Map.of(
                "register_api_path", "/api/v1/auth/registration/" + verificationEntryList.get(pesel).getRegistrationToken(),
                "name", "Mikołaj",
                "surname", "Kamiński"
        );
    }

    private void checkIfRegistrationCodeIsCorrect(RegistrationCode registrationCode, String code){
        if(!registrationCode.getCode().equals(code)){
            log.error("IncorrectRegistrationCodeException: registrationCode: '" + registrationCode + "', code: " + code + "'");
            throw new IncorrectRegistrationCodeException("Registration code is incorrect");
        }
    }

    private void checkIfRegistrationCodeExpired(RegistrationCode registrationCode){
        if(registrationCode.isExpire()){
            log.error("RegistrationCodeExpiredException: " + registrationCode.getHowManyCodeExistsInSeconds() + "s > " + registrationCode.getExpireIn() + "s");
            throw new RegistrationCodeExpiredException("Token expired, cause of " + registrationCode.getHowManyCodeExistsInSeconds() + "s > " + registrationCode.getExpireIn() + "s");
        }
    }

    private void checkIfPeselExists(String pesel){
        //TODO: check in remote API
        if(false){
            log.error("PeselDoesNotExistsException: PESEL: '" + pesel);
            verificationEntryList.remove(pesel);
            throw new PeselDoesNotExistsException("Pesel does not exists!");
        }
    }

    private void checkIfAlreadyRegistered(String pesel){
        //TODO: user service check
        if(false){
            log.error("UserAlreadyRegisteredException: PESEL: '" + pesel);
            verificationEntryList.remove(pesel);
            throw new UserAlreadyRegisteredException("Person with that pesel is already registered");
        }
    }

    private RegistrationCode createRegistrationCode() {
        String code = RandomStringUtils.randomNumeric(6);
        return new RegistrationCode(code, 60);
    }
}
