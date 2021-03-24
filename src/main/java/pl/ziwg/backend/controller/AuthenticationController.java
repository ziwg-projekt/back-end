package pl.ziwg.backend.controller;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.ApiError;
import pl.ziwg.backend.exception.IncorrectRegistrationCodeException;
import pl.ziwg.backend.exception.NotExistingPeselException;
import pl.ziwg.backend.exception.PeselNotCorrespondingToTokenException;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.notificator.NotificationType;
import pl.ziwg.backend.security.RegistrationCode;
import pl.ziwg.backend.security.VerificationEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private Map<String, VerificationEntry> verificationEntryList = new HashMap<>();

    @GetMapping("/registration-code/generate")
    public ResponseEntity<Map<String, String>> generateRegistrationCodeForPesel(@RequestBody Map<String, Object> registrationDetails) {
        final String pesel = (String) registrationDetails.get("pesel");
        final NotificationType notificationType = NotificationType.values()[(int) registrationDetails.get("notification_type")];
        System.out.println("Nowy request " + pesel + " i typ: " + notificationType.toString());
        Map<String, String> endpointToVerify = new HashMap<>();
        VerificationEntry verificationEntry = new VerificationEntry(createRegistrationCode(), "1834278413931413208831");
        verificationEntryList.put(pesel, verificationEntry);
        //TODO: whole logic, add notification and token generation
        endpointToVerify.put("verify_link", "/api/v1/auth/registration-code/verify/"+verificationEntry.getToken());
        return new ResponseEntity<>(endpointToVerify, HttpStatus.OK);
    }

    @GetMapping("/registration-code/verify/{token}")
    public ResponseEntity<Map<String, String>> verifyRegistrationCode(@RequestBody Map<String, Object> peselMap, @PathVariable String token) {
        final String pesel = (String) peselMap.get("pesel");
        final String registrationCode = (String) peselMap.get("registration_code");
        if(verificationEntryList.get(pesel)==null){
            throw new NotExistingPeselException("Pesel doesn't exist, validate it");
        }
        if(verificationEntryList.get(pesel).getToken().equals(token)){
            if(verificationEntryList.get(pesel).getRegistrationCode().getCode().equals(registrationCode)){
                Map<String, String> person = new HashMap<>();
                person.put("name", "Mikołaj");
                person.put("surname", "Kamiński");
                return new ResponseEntity<>(person, HttpStatus.OK);
            }
            else{
                throw new IncorrectRegistrationCodeException("Registration code is incorrect");
            }
        }
        else{
            throw new PeselNotCorrespondingToTokenException("Token is not corresponding to pesel");
        }
    }

    private RegistrationCode createRegistrationCode() {
        String code = RandomStringUtils.randomNumeric(6);
        return new RegistrationCode(code, 60);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(Exception exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }
}
