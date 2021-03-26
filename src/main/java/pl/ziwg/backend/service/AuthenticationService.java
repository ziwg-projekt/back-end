package pl.ziwg.backend.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.convert.EntityConverter;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.exception.*;
import pl.ziwg.backend.externalapi.governmentapi.Person;
import pl.ziwg.backend.externalapi.governmentapi.PersonRegister;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.notificator.CommunicationChannelType;
import pl.ziwg.backend.requestbody.RegistrationRequestBody;
import pl.ziwg.backend.security.RegistrationCode;
import pl.ziwg.backend.security.VerificationEntry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class AuthenticationService {
    protected static final Logger log = Logger.getLogger(AuthenticationService.class);
    private Map<Person, VerificationEntry> verificationEntryList = new HashMap<>();
    private EmailService emailService;
    private SMSService smsService;
    private PersonRegister personRegister;

    @Autowired
    public AuthenticationService(EmailService emailService, PersonRegister personRegister, SMSService smsService){
        this.emailService = emailService;
        this.personRegister = personRegister;
        this.smsService = smsService;
    }

    public void checkIfCorrectRegistrationCodeRequestBody(Map<String, String> verificationDetails){
        if(!verificationDetails.containsKey("registration_code")){
            log.error("Request body should contain JSON with 'pesel' and 'communication_channel_type' keys: " + verificationDetails.toString());
            throw new IncorrectPayloadSyntaxException("Request body should contain JSON with 'registration_code' key");
        }
    }

    public void checkIfCorrectRegistrationRequestBody(Map<String, String> userData){
        if(!userData.containsKey("password")){
            log.error("Request body should contain JSON with 'pesel' and 'communication_channel_type' keys: " + userData.toString());
            throw new IncorrectPayloadSyntaxException("Request body should contain JSON with 'password' key");
        }
    }

    public Map<String, String> doVerificationProcess(RegistrationRequestBody registrationDetails){
        String pesel = registrationDetails.getPesel();
        validateIfRegistrationIsPossible(pesel);
        Person person = personRegister.getPersonByPesel(pesel);
        Map.Entry<Person, VerificationEntry> entry =  generateVerificationEntry(person);
        sendCodeThroughChosenCommunicationChannel(person, registrationDetails.getCommunicationChannelType(), entry.getValue().getRegistrationCode().getCode());
        return Map.of("verify_api_path", "/api/v1/auth/registration/code/verify/" + entry.getValue().getVerificationToken());
    }

    public Map<String, Object> verifyRegistrationCodeCorrectness(Map<String, String> verificationDetails, String verificationToken){
        Map.Entry<Person, VerificationEntry> entry = getMapEntryByVerificationToken(verificationToken);
        validateIfVerificationSuccessful(entry, verificationDetails.get("registration_code"));
        return allowRegistration(entry);
    }

    public void registerUser(String registrationToken, Map<String, String> userData){
        Map.Entry<Person, VerificationEntry> entry = getMapEntryByRegistrationToken(registrationToken);
        String password = getPasswordFromUserData(userData);
        //TODO: make registration
        verificationEntryList.remove(entry.getKey());
    }

    private void validateIfRegistrationIsPossible(String pesel){
        checkIfPeselExists(pesel);
        checkIfAlreadyRegistered(pesel);
    }

    private void validateIfVerificationSuccessful(Map.Entry<Person, VerificationEntry> entry, String registrationCode){
        checkIfRegistrationAlreadyVerified(entry);
        checkIfRegistrationCodeIsCorrect(entry.getValue().getRegistrationCode(), registrationCode);
        checkIfRegistrationCodeExpired(entry.getValue().getRegistrationCode());
    }

    private void sendCodeThroughChosenCommunicationChannel(Person person, CommunicationChannelType communicationChannelType, String code){
        switch(communicationChannelType) {
            case EMAIL:
                Optional<String> email = person.getEmail();
                if(email.isPresent()) {
                    emailService.sendVerificationCode(email.get(), code);
                }
                else{
                    verificationEntryList.remove(person);
                    throw new NotSupportedCommunicationChannelException("Person with pesel '" + person.getPesel() + "' has no email assigned!") ;
                }
                break;

            case SMS:
                Optional<String> phoneNumber = person.getPhoneNumber();
                if(phoneNumber.isPresent()) {
                    smsService.sendVerificationCode(phoneNumber.get(), code);
                }
                else{
                    verificationEntryList.remove(person);
                    throw new NotSupportedCommunicationChannelException("Person with pesel '" + person.getPesel() + "' has no phone number assigned!") ;
                }
                break;
        }
    }

    private void checkIfRegistrationAlreadyVerified(Map.Entry<Person, VerificationEntry> entry){
        if(entry.getValue().isVerified()){
            log.error("Verification is already done, PESEL: '" + entry.getKey().getPesel() + "'");
            throw new VerificationAlreadySucceededException("That verification was done before and there is no possibility to generate registration code one more time");
        }
    }

    private String getPasswordFromUserData(Map<String, String> userData){
        try {
            return userData.get("password");
        } catch (ClassCastException | ArrayIndexOutOfBoundsException e){
            log.error("InvalidDataTypeInPayloadException : " + e.getMessage());
            throw new InvalidDataTypeInPayloadException("Field 'password' should be String!");
        }
    }

    private Map.Entry<Person, VerificationEntry> generateVerificationEntry(Person person){
        VerificationEntry verificationEntry = new VerificationEntry(createRegistrationCode(), RandomStringUtils.randomAlphanumeric(30));
        verificationEntryList.put(person, verificationEntry);
        return getMapEntryByVerificationToken(verificationEntry.getVerificationToken());
    }

    private Map.Entry<Person, VerificationEntry> getMapEntryByVerificationToken(String token){
        for (Map.Entry<Person,VerificationEntry> entry : verificationEntryList.entrySet()){
            if(entry.getValue().getVerificationToken().equals(token)){
                return entry;
            }
        }
        log.error("TokenDoesNotExistsException: verification token: " + token + "'");
        throw new TokenDoesNotExistsException("There is no such a verification token");
    }

    private Map.Entry<Person, VerificationEntry> getMapEntryByRegistrationToken(String token){
        for (Map.Entry<Person, VerificationEntry> entry : verificationEntryList.entrySet()){
            if(entry.getValue().getRegistrationToken().equals(token)){
                return entry;
            }
        }
        log.error("TokenDoesNotExistsException: registration token: " + token + "'");
        throw new TokenDoesNotExistsException("There is no such a registration token");
    }

    private Map<String, Object> allowRegistration(Map.Entry<Person, VerificationEntry> entry){
        entry.getValue().setVerified(true);
        entry.getValue().setRegistrationToken(RandomStringUtils.randomAlphanumeric(30));
        log.info("Registration succeeded: PESEL: '" + entry.getKey().getPesel());
        Map<String, Object> response = EntityToMapConverter.getRepresentationWithChosenFields(entry.getKey(), Arrays.asList("name", "surname", "pesel", "phoneNumber", "email"));
        response.put("register_api_path", "/api/v1/auth/registration/" + entry.getValue().getRegistrationToken());
        return response;
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
        if(!personRegister.checkIfPeselExists(pesel)){
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
