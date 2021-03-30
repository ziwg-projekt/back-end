package pl.ziwg.backend.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.exception.IncorrectRegistrationCodeException;
import pl.ziwg.backend.exception.NotSupportedCommunicationChannelException;
import pl.ziwg.backend.exception.PeselDoesNotExistsException;
import pl.ziwg.backend.exception.RegistrationCodeExpiredException;
import pl.ziwg.backend.exception.TokenDoesNotExistsException;
import pl.ziwg.backend.exception.UserAlreadyRegisteredException;
import pl.ziwg.backend.exception.UsernameNotAvailableException;
import pl.ziwg.backend.exception.VerificationAlreadySucceededException;
import pl.ziwg.backend.externalapi.governmentapi.Person;
import pl.ziwg.backend.externalapi.governmentapi.PersonRegister;
import pl.ziwg.backend.jsonbody.request.*;
import pl.ziwg.backend.jsonbody.request.CitizenRegistrationRequestBody;
import pl.ziwg.backend.jsonbody.request.HospitalRegistrationRequestBody;
import pl.ziwg.backend.jsonbody.request.RegistrationCodeRequestBody;
import pl.ziwg.backend.jsonbody.request.VerifyCodeRequestBody;
import pl.ziwg.backend.jsonbody.response.AllowRegistrationResponse;
import pl.ziwg.backend.jsonbody.response.JwtResponse;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.entity.Role;
import pl.ziwg.backend.model.entity.RoleName;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.model.repository.CitizenRepository;
import pl.ziwg.backend.model.repository.HospitalRepository;
import pl.ziwg.backend.model.repository.RoleRepository;
import pl.ziwg.backend.model.repository.UserRepository;
import pl.ziwg.backend.notificator.CommunicationChannelType;
import pl.ziwg.backend.notificator.email.EmailSubject;
import pl.ziwg.backend.security.RegistrationCode;
import pl.ziwg.backend.security.VerificationEntry;
import pl.ziwg.backend.security.jwt.JwtProvider;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Transactional
@Service
public class AuthenticationService {
    protected static final Logger log = Logger.getLogger(AuthenticationService.class);
    private Map<Person, VerificationEntry> verificationEntryList = new HashMap<>();
    private EmailService emailService;
    private SMSService smsService;
    private PersonRegister personRegister;
    private UserRepository userRepository;
    private CitizenRepository citizenRepository;
    private HospitalRepository hospitalRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder encoder;
    private AuthenticationManager authenticationManager;
    private JwtProvider jwtProvider;

    @Autowired
    public AuthenticationService(EmailService emailService,
                                 PersonRegister personRegister,
                                 SMSService smsService,
                                 UserRepository userRepository,
                                 CitizenRepository citizenRepository,
                                 HospitalRepository hospitalRepository,
                                 RoleRepository roleRepository,
                                 PasswordEncoder encoder,
                                 AuthenticationManager authenticationManager,
                                 JwtProvider jwtProvider){
        this.emailService = emailService;
        this.personRegister = personRegister;
        this.smsService = smsService;
        this.userRepository = userRepository;
        this.citizenRepository = citizenRepository;
        this.hospitalRepository = hospitalRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    public Map<String, String> doVerificationProcess(RegistrationCodeRequestBody registrationDetails){
        String pesel = registrationDetails.getPesel();
        validateIfRegistrationIsPossible(pesel);
        Person person = personRegister.getPersonByPesel(pesel);
        Map.Entry<Person, VerificationEntry> entry =  generateVerificationEntry(person);
        sendCodeThroughChosenCommunicationChannel(person, registrationDetails.getCommunicationChannelType(), entry.getValue().getRegistrationCode().getCode());
        return Map.of("verify_api_path", "/api/v1/auth/registration/citizen/verify?token=" + entry.getValue().getVerificationToken());
    }

    public AllowRegistrationResponse verifyRegistrationCodeCorrectness(VerifyCodeRequestBody registrationCode, String verificationToken){
        Map.Entry<Person, VerificationEntry> entry = getMapEntryByVerificationToken(verificationToken);
        validateIfVerificationSuccessful(entry, registrationCode.getRegistrationCode());
        return allowRegistration(entry);
    }

    public void registerUser(String registrationToken, CitizenRegistrationRequestBody userData){
        log.info("Current verification list = " + verificationEntryList.toString());
        Person person = getPersonByRegistrationToken(registrationToken);
        checkIfUsernameAvailable(userData.getUsername());
        Address address = new Address(userData.getCity(), userData.getStreet(), userData.getStreetNumber());
        User user = new User(userData.getUsername(), encoder.encode(userData.getPassword()), new Citizen(person, address));
        user.setRoles(new HashSet<>(Collections.singletonList(roleRepository.findByName(RoleName.ROLE_CITIZEN).get())));
        this.userRepository.save(user);
        log.info("Successful registration for user citizen with pesel '" + person.getPesel() + "', username + '" + userData.getUsername() +"' and roles: " + user.getRoles());
        verificationEntryList.remove(person);
        log.info("Current verification list = " + verificationEntryList.toString());
    }

    public void registerHospital(HospitalRegistrationRequestBody hospitalData){
        checkIfUsernameAvailable(hospitalData.getUsername());
        Address address = new Address(hospitalData.getCity(), hospitalData.getStreet(), hospitalData.getStreetNumber());
        Hospital hospital = new Hospital(hospitalData.getHospitalName(), address);
        User user = new User(hospitalData.getUsername(), encoder.encode(hospitalData.getPassword()), hospital);
        log.info(user.toString());
        user.setRoles(new HashSet<>(Collections.singletonList(roleRepository.findByName(RoleName.ROLE_HOSPITAL).get())));
        this.userRepository.save(user);
        log.info("Successful registration for user hospital with username + '" + hospitalData.getUsername() +"' and roles: " + user.getRoles());

    }

    public ResponseEntity<JwtResponse> loginUser(LoginRequestBody userData){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userData.getUsername(), userData.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("Successful login for user with username '" + userData.getUsername() + "'");
        JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities());
        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }

    public void checkIfUsernameAvailable(String username){
        if(userRepository.existsByUsername(username)){
            throw new UsernameNotAvailableException("Username '" + username + "' is in use!");
        }
    }

    public void showCurrentState(){
        log.info("Current users list : " + userRepository.findAll().toString());
        log.info("Current citizen list : " + citizenRepository.findAll().toString());
        log.info("Current hospital list : " + hospitalRepository.findAll().toString());
    }

    public boolean checkIfAllRolesPresent(){
        for (RoleName roleName : RoleName.values()) {
            Optional<Role> role = roleRepository.findByName(roleName);
            if(role.isEmpty()){
                log.error("Role " + roleName.toString() +" not found!");
                return false;
            }
            else{
                log.info("Role " + roleName.toString() + " was found");
            }
        }
        return true;
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

    public void sendCodeThroughChosenCommunicationChannel(Person person, CommunicationChannelType communicationChannelType, String code){
        log.info("Sending registration code '" + code + "' to citizen with pesel '"  + person.getPesel() + "' via " + communicationChannelType.toString());
        switch(communicationChannelType) {
            case EMAIL:
                Optional<String> email = person.getEmail();
                if(email.isPresent()) {
                    log.info("Email for '" + person.getPesel() +"'was found - '" + email.get() + "'");
                    emailService.sendVerificationCode(email.get(), code, EmailSubject.VERIFICATION_CODE,
                            person.getName());
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

    private Person getPersonByRegistrationToken(String token){
        for (Map.Entry<Person, VerificationEntry> entry : verificationEntryList.entrySet()){
            if(entry.getValue().getRegistrationToken().equals(token)){
                return entry.getKey();
            }
        }
        log.error("TokenDoesNotExistsException: registration token: " + token + "'");
        throw new TokenDoesNotExistsException("There is no such a registration token");
    }

    private AllowRegistrationResponse allowRegistration(Map.Entry<Person, VerificationEntry> entry){
        entry.getValue().setVerified(true);
        entry.getValue().setRegistrationToken(RandomStringUtils.randomAlphanumeric(30));
        log.info("Verification for registration succeeded: PESEL: '" + entry.getKey().getPesel() + "'");
        return new AllowRegistrationResponse(entry.getValue().getRegistrationToken(), entry.getKey());
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
            throw new PeselDoesNotExistsException("Pesel does not exists!");
        }
    }

    private void checkIfAlreadyRegistered(String pesel){
        if(citizenRepository.existsByPesel(pesel)){
            log.error("UserAlreadyRegisteredException: PESEL: '" + pesel);
            throw new UserAlreadyRegisteredException("Person with that pesel is already registered");
        }
    }

    private RegistrationCode createRegistrationCode() {
        String code = RandomStringUtils.randomNumeric(6);
        return new RegistrationCode(code, 60);
    }
}
