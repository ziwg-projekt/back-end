package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.jsonbody.request.FinalRegistrationRequestBody;
import pl.ziwg.backend.jsonbody.request.RegistrationCodeRequestBody;
import pl.ziwg.backend.jsonbody.request.RegistrationRequestBody;
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

    @PostMapping("/registration/code/generate")
    public ResponseEntity<Map<String, String>> generateToken(@Valid @RequestBody RegistrationRequestBody registrationDetails) {
        Map<String, String> apiPathToVerify = authenticationService.doVerificationProcess(registrationDetails);
        return new ResponseEntity<>(apiPathToVerify, HttpStatus.OK);
    }

    @PostMapping("/registration/code/verify/{token}")
    public ResponseEntity<AllowRegistrationResponse> verifyRegistrationCode(@Valid @RequestBody RegistrationCodeRequestBody registrationCode, @PathVariable String token) {
        AllowRegistrationResponse response = authenticationService.verifyRegistrationCodeCorrectness(registrationCode, token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/registration/{token}")
    @ResponseStatus(HttpStatus.OK)
    public void registerUser(@Valid @RequestBody FinalRegistrationRequestBody userData, @PathVariable String token){
        authenticationService.registerUser(token, userData);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginUser(@Valid @RequestBody FinalRegistrationRequestBody userData){
        return authenticationService.loginUser(userData);
    }

}
