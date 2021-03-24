package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.*;
import pl.ziwg.backend.notificator.NotificationType;
import pl.ziwg.backend.security.VerificationEntry;
import pl.ziwg.backend.service.AuthenticationService;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private AuthenticationService authenticationService;

    @Autowired
    AuthenticationController(AuthenticationService authenticationService){
        this.authenticationService = authenticationService;
    }

    @PostMapping("/registration-code/generate")
    public ResponseEntity<Map<String, String>> generateRegistrationCodeForPesel(@RequestBody Map<String, Object> registrationDetails) {
        Map<String, String> apiPathToVerify = Map.of(
                "verify_api_path", "/api/v1/auth/registration-code/verify/" + authenticationService.generateRegistrationCode(registrationDetails)
        );
        return new ResponseEntity<>(apiPathToVerify, HttpStatus.OK);
    }

    @PostMapping("/registration-code/verify/{token}")
    public ResponseEntity<Map<String, String>> verifyRegistrationCode(@RequestBody Map<String, Object> peselMap, @PathVariable String token) {
        final String pesel = (String) peselMap.get("pesel");
        final String registrationCode = (String) peselMap.get("registration_code");
        //TODO: verify parsing in 2 lines above
        Map<String, String> response = authenticationService.verifyRegistrationCode(pesel, registrationCode, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(PeselNotCorrespondingToTokenException.class)
    public ResponseEntity<ApiError> handlePeselNotCorrespondingToTokenException(PeselNotCorrespondingToTokenException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(IncorrectRegistrationCodeException.class)
    public ResponseEntity<ApiError> handleIncorrectRegistrationCodeException(IncorrectRegistrationCodeException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(RegistrationCodeExpiredException.class)
    public ResponseEntity<ApiError> handleRegistrationCodeExpiredException(RegistrationCodeExpiredException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(NoCodeForGivenPeselException.class)
    public ResponseEntity<ApiError> handleNoCodeForGivenPeselException(NoCodeForGivenPeselException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(PeselDoesNotExistsException.class)
    public ResponseEntity<ApiError> handlePeselDoesNotExistsException(PeselDoesNotExistsException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequestException(InvalidRequestException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }





}
