package pl.ziwg.backend.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.dto.HospitalCitizenRegisterDto;
import pl.ziwg.backend.exception.IncorrectRegistrationCodeException;
import pl.ziwg.backend.exception.NotSupportedCommunicationChannelException;
import pl.ziwg.backend.exception.PeselDoesNotExistsException;
import pl.ziwg.backend.exception.RegistrationCodeExpiredException;
import pl.ziwg.backend.exception.ResourceNotFoundException;
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
import pl.ziwg.backend.model.enumerates.CitizenState;
import pl.ziwg.backend.model.repository.CitizenRepository;
import pl.ziwg.backend.model.repository.HospitalRepository;
import pl.ziwg.backend.model.repository.RoleRepository;
import pl.ziwg.backend.model.repository.UserRepository;
import pl.ziwg.backend.notificator.CommunicationChannelType;
import pl.ziwg.backend.notificator.email.EmailSubject;
import pl.ziwg.backend.security.RegistrationCode;
import pl.ziwg.backend.security.VerificationEntry;
import pl.ziwg.backend.security.jwt.JwtProvider;
import pl.ziwg.backend.security.jwt.service.UserPrinciple;

import javax.transaction.Transactional;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

@Transactional
@Service
public class AuthenticationService {
    protected static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private Map<Person, VerificationEntry> verificationEntryList = new HashMap<>();
    private EmailService emailService;
    private SMSService smsService;
    private PersonRegister personRegister;
    private UserService userService;
    private CitizenService citizenService;
    private HospitalService hospitalService;
    private RoleService roleService;
    private AuthenticationManager authenticationManager;
    private JwtProvider jwtProvider;

    @Autowired
    public AuthenticationService(EmailService emailService,
                                 PersonRegister personRegister,
                                 SMSService smsService,
                                 UserService userService,
                                 CitizenService citizenService,
                                 HospitalService hospitalService,
                                 RoleService roleService,
                                 AuthenticationManager authenticationManager,
                                 JwtProvider jwtProvider){
        this.emailService = emailService;
        this.personRegister = personRegister;
        this.smsService = smsService;
        this.userService = userService;
        this.citizenService = citizenService;
        this.hospitalService = hospitalService;
        this.roleService = roleService;
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    public Map<String, String> doVerificationProcess(RegistrationCodeRequestBody registrationDetails){
        String pesel = getPeselIfValid(registrationDetails);
        checkIfAlreadyRegistered(pesel);
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

    public void registerCitizenByToken(String registrationToken, CitizenRegistrationRequestBody userData){
        log.info("Current verification list = " + verificationEntryList.toString());
        Person person = getPersonByRegistrationToken(registrationToken);
        registerPersonInSystem(person, userData);
        log.info("Successful registration for user citizen with pesel '" + person.getPesel() + "', username + '" + userData.getUsername());
        verificationEntryList.remove(person);
        log.info("Current verification list = " + verificationEntryList.toString());
    }

    public void registerPersonInSystem(Person person, CitizenRegistrationRequestBody userData){
        checkIfUsernameAvailable(userData.getUsername());
        Address address = new Address(userData.getCity(), userData.getStreet(), userData.getStreetNumber());
        userService.saveCitizen(userData.getUsername(), userData.getPassword(), new Citizen(person, address));
    }

    public void registerCitizenByHospital(final String pesel, final CitizenRegistrationRequestBody userData) {
        checkIfAlreadyRegistered(pesel);
        Person person = personRegister.getPersonByPesel(pesel);
        registerPersonInSystem(person, userData);
        log.info("Successful registration by hospital for user citizen with pesel '" + person.getPesel() + "', username + '" + userData.getUsername());
    }

    public void registerHospital(HospitalRegistrationRequestBody hospitalData){
        checkIfUsernameAvailable(hospitalData.getUsername());
        Address address = new Address(hospitalData.getCity(), hospitalData.getStreet(), hospitalData.getStreetNumber());
        userService.saveHospital(hospitalData.getUsername(), hospitalData.getPassword(), new Hospital(hospitalData.getHospitalName(), address));
        log.info("Successful registration for user hospital with username + '" + hospitalData.getUsername());

    }

    public ResponseEntity<JwtResponse> loginUser(LoginRequestBody userData){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userData.getUsername(), userData.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtProvider.generateJwtToken(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        log.info("Successful login for user with username '" + userData.getUsername() + "'");

        JwtResponse jwtResponse = new JwtResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities(), userService.findByUsername(userDetails.getUsername()).get().getId());
        return new ResponseEntity<>(jwtResponse, HttpStatus.OK);
    }

    public void checkIfUsernameAvailable(String username){
        if(userService.checkIfUserExists(username)){
            throw new UsernameNotAvailableException("Username '" + username + "' is in use!");
        }
    }

    public void showCurrentState(){
        log.info("Current users list : " + userService.findAll().toString());
        log.info("Current citizen list : " + citizenService.findAll().toString());
        log.info("Current hospital list : " + hospitalService.findAll().toString());
    }

    public boolean checkIfAllRolesPresent(){
        for (RoleName roleName : RoleName.values()) {
            Optional<Role> role = roleService.findByName(roleName);
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

    private String getPeselIfValid(RegistrationCodeRequestBody registrationCodeRequestBody){
        if(registrationCodeRequestBody.getPesel().length() == 11){
            return registrationCodeRequestBody.getPesel();
        }
        else{
            throw new PeselDoesNotExistsException("Pesel should have 11 digits, was given: " + registrationCodeRequestBody.getPesel().length());
        }
    }

    private void validateIfVerificationSuccessful(Map.Entry<Person, VerificationEntry> entry, String registrationCode){
        checkIfRegistrationAlreadyVerified(entry);
        checkIfRegistrationCodeIsCorrect(entry.getValue().getRegistrationCode(), registrationCode);
        checkIfRegistrationCodeExpired(entry.getValue().getRegistrationCode());
    }

    private Citizen createCitizen(final HospitalCitizenRegisterDto hospitalCitizenRegisterDto, final Address address,
                                  final Person person) {
        final Citizen citizen = new Citizen();
        citizen.setEmail(hospitalCitizenRegisterDto.getEmail());
        citizen.setName(person.getName());
        citizen.setSurname(person.getSurname());
        citizen.setPesel(person.getPesel());
        citizen.setAddress(address);
        citizen.setPhoneNumber(hospitalCitizenRegisterDto.getPhoneNumber());
        citizen.setState(CitizenState.WAITING);
        citizen.setHospital(getUserFromContext().getHospital());
        return citizen;
    }

    private User getUserFromContext() {
        final UserPrinciple up = (UserPrinciple)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final Optional<User> optionalUser = userService.findById(up.getId());
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new ResourceNotFoundException("id", "user");
        }
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
                    log.info("Email to '" + person.getPesel() + "' successfully send!");
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
            System.out.println(entry);
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

    private void checkIfAlreadyRegistered(String pesel){
        if(citizenService.checkIfExistsByPesel(pesel)){
            log.error("UserAlreadyRegisteredException: PESEL: '" + pesel);
            throw new UserAlreadyRegisteredException("Person with that pesel is already registered");
        }
    }

    private RegistrationCode createRegistrationCode() {
        String code = RandomStringUtils.randomNumeric(6);
        return new RegistrationCode(code, 60);
    }
}
