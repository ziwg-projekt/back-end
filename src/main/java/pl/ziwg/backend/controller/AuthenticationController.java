package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.jsonbody.request.CitizenRegistrationRequestBody;
import pl.ziwg.backend.jsonbody.request.HospitalRegistrationRequestBody;
import pl.ziwg.backend.jsonbody.request.VerifyCodeRequestBody;
import pl.ziwg.backend.jsonbody.request.RegistrationCodeRequestBody;
import pl.ziwg.backend.jsonbody.response.AllowRegistrationResponse;
import pl.ziwg.backend.jsonbody.response.JwtResponse;
import pl.ziwg.backend.service.AuthenticationService;

import javax.validation.Valid;
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

    @PostMapping("/registration/citizen/notify")
    public ResponseEntity<Map<String, String>> sendCodeToCitizen(@Valid @RequestBody RegistrationCodeRequestBody registrationDetails) {
        Map<String, String> apiPathToVerify = authenticationService.doVerificationProcess(registrationDetails);
        return new ResponseEntity<>(apiPathToVerify, HttpStatus.OK);
    }

    @PostMapping("/registration/citizen/verify")
    public ResponseEntity<AllowRegistrationResponse> verifyRegistrationCode(@Valid @RequestBody VerifyCodeRequestBody registrationCode, @RequestParam String token) {
        AllowRegistrationResponse response = authenticationService.verifyRegistrationCodeCorrectness(registrationCode, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/registration/citizen/register")
    @ResponseStatus(HttpStatus.OK)
    public void registerCitizen(@Valid @RequestBody CitizenRegistrationRequestBody userData, @RequestParam String token){
        authenticationService.registerUser(token, userData);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/registration/hospital/register")
    @ResponseStatus(HttpStatus.OK)
    public void registerHospital(@Valid @RequestBody HospitalRegistrationRequestBody hospitalData){
        authenticationService.registerHospital(hospitalData);
    }


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginUser(@Valid @RequestBody CitizenRegistrationRequestBody userData){
        return authenticationService.loginUser(userData);
    }

}
