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

    @PostMapping("/registration/code/generate")
    public ResponseEntity<Map<String, String>> generateRegistrationCodeForPesel(@RequestBody Map<String, Object> registrationDetails) {
        authenticationService.checkIfCorrectGenerationCodeRequestBody(registrationDetails);
        Map<String, String> apiPathToVerify = authenticationService.sendVerificationCodeToUser(registrationDetails);
        return new ResponseEntity<>(apiPathToVerify, HttpStatus.OK);
    }

    @PostMapping("/registration/code/verify/{token}")
    public ResponseEntity<Map<String, String>> verifyRegistrationCode(@RequestBody Map<String, String> verificationDetails, @PathVariable String token) {
        authenticationService.checkIfCorrectRegistrationCodeRequestBody(verificationDetails);
        Map<String, String> response = authenticationService.verifyRegistrationCodeCorrectness(verificationDetails, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/registration/{token}")
    @ResponseStatus(HttpStatus.OK)
    public void registerUser(@RequestBody Map<String, Object> userData, @PathVariable String token){
        authenticationService.registerUser(token, userData);
    }

    @ExceptionHandler(IncorrectRegistrationCodeException.class)
    public ResponseEntity<ApiError> handleIncorrectRegistrationCodeException(IncorrectRegistrationCodeException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RegistrationCodeExpiredException.class)
    public ResponseEntity<ApiError> handleRegistrationCodeExpiredException(RegistrationCodeExpiredException exception) {
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

    @ExceptionHandler(TokenDoesNotExistsException.class)
    public ResponseEntity<ApiError> handleTokenDoesNotExistsException(TokenDoesNotExistsException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> handleUserAlreadyRegisteredException(UserAlreadyRegisteredException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(VerificationAlreadySucceededException.class)
    public ResponseEntity<ApiError> handleVerificationAlreadySucceededException(VerificationAlreadySucceededException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.UNAUTHORIZED);
    }






}
