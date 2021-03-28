package pl.ziwg.backend.controller;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.ziwg.backend.service.AuthenticationService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthenticationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private AuthenticationService authenticationService;

    protected static final Logger log = Logger.getLogger(AuthenticationService.class);

    private String pesel = "96050834215";
    private String code = "123456";
    private String password = "admin12345";
    private String username = "admintest";

    @Autowired
    private TestRestTemplate restTemplate;

    private String changeString(String stringToChange){
        int index = stringToChange.length() / 2;
        return stringToChange.substring(0, index) + 'x' + stringToChange.substring(index + 1);
    }
    
    private String getHumanReadableResponse(ResponseEntity responseEntity){
        return "Status code = " + responseEntity.getStatusCode() + ", body = " + responseEntity.getBody();
    }

    private ResponseEntity<Map> doPost(String path, Map body){
        return restTemplate.postForEntity("http://localhost:" + port + path, body, Map.class);
    }

    @BeforeEach
    public void showCurrentStateBefore(){
        try{
            authenticationService.deleteUser(username);
            authenticationService.deleteCitizen(pesel);
        } catch(Exception ex){
            log.info(ex.getMessage());
        }

        log.info("STARTING NEW TEST CASE");
        authenticationService.showCurrentState();
    }

    @AfterEach
    public void showCurrentStateAfter(){
        log.info("ENDING TEST CASE");

//        try{
//            authenticationService.deleteUser(username);
//            authenticationService.deleteCitizen(pesel);
//        } catch(Exception ex){
//            log.info(ex.getMessage());
//        }
        authenticationService.showCurrentState();
    }

    private String basicCodeGeneration(String pesel){
        ResponseEntity<Map> responseEntity = doPost("/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1));
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) responseEntity.getBody().get("verify_api_path");
    }

    private String basicVerification(String path){
        ResponseEntity<Map> responseEntity = doPost(path, Map.of("registration_code", code));
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        return (String) responseEntity.getBody().get("register_api_path");
    }

    private void basicRegistration(String path){
        ResponseEntity<Map> responseEntity = doPost(path, Map.of("password", password, "username", username));
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void failLogin(){
        ResponseEntity<Map> responseEntity = doPost("/api/v1/auth/login", Map.of("password", password, "username", username));
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("BadCredentialsException");
    }

    @Test
    public void passLogin(){
        String path = basicCodeGeneration(pesel);
        path = basicVerification(path);
        basicRegistration(path);

        ResponseEntity<Map> responseEntity = doPost("/api/v1/auth/login", Map.of("password", password, "username", username));
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void goThroughEntireRegistrationProcess() {
        String path = basicCodeGeneration(pesel);
        path = basicVerification(path);
        basicRegistration(path);
    }

    @Test
    public void invokeIncorrectRegistrationCodeExceptionByChangingCode() {
        String path = basicCodeGeneration(pesel);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", changeString(code)), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("IncorrectRegistrationCodeException");
    }

    @Test
    public void invokeUserAlreadyRegisteredException(){
        String path = basicCodeGeneration(pesel);
        path = basicVerification(path);
        basicRegistration(path);
        //TODO: refactor test, make more methods for sending post to avoid something like a from a line below
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("UserAlreadyRegisteredException");
    }

    @Test
    public void invokeUsernameNotAvailableException(){
        String path = basicCodeGeneration(pesel);
        path = basicVerification(path);
        basicRegistration(path);

        path = basicCodeGeneration(changeString(pesel));
        path = basicVerification(path);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("password", password, "username", username), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("UsernameNotAvailableException");
    }

    @Test
    public void invokeVerificationTokenDoesNotExistsExceptionByChangingToken() {
        String path = basicCodeGeneration(pesel);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path + "x", Map.of("registration_code", code), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("TokenDoesNotExistsException");
    }


    @Test
    public void invokeRegistrationTokenDoesNotExistsExceptionByChangingToken() {
        String path = basicCodeGeneration(pesel);
        path = basicVerification(path);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path + "x", Map.of("password", password, "username", username), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("TokenDoesNotExistsException");
    }

    @Test
    public void invokeRegistrationTokenDoesNotExistsExceptionByDoubleConfirm() {
        String path = basicCodeGeneration(pesel);
        path = basicVerification(path);
        basicRegistration(path);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("password", password, "username", username), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("TokenDoesNotExistsException");
    }

    @Test
    public void invokeVerificationAlreadySucceededExceptionByDoubleConfirm() {
        String path = basicCodeGeneration(pesel);
        basicVerification(path);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("VerificationAlreadySucceededException");
    }

    @Test
    public void invokeMethodArgumentNotValidExceptionByPuttingWrongKeyInRequestBody_v1() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of(changeString("pesel"), pesel,"communication_channel_type", 1), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("MethodArgumentNotValidException");
    }

    @Test
    public void invokeMethodArgumentNotValidExceptionByPuttingWrongKeyInRequestBody_v2() {
        String path = basicCodeGeneration(pesel);
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of(changeString("registration_code"), code), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("MethodArgumentNotValidException");
    }

    @Test
    public void invokeMethodArgumentNotValidExceptionByPuttingWrongKeyInRequestBody_v3() {
        String path = basicCodeGeneration(pesel);
        path = basicVerification(path);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of(changeString("password"), password, "username", username), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("MethodArgumentNotValidException");
    }

    @Test
    public void invokeHttpMessageNotReadableExceptionBySendingEmptyRequestBody() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", "", Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("HttpMediaTypeNotSupportedException");
    }


    @Test
    public void invokeHttpMessageNotReadableExceptionByWrappingCodeInExtraMap() {
        String path = basicCodeGeneration(pesel);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code",  Map.of("registration_code", code)), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("HttpMessageNotReadableException");
    }

    @Disabled
    @Test
    public void invokeRegistrationCodeExpiredExceptionBySleepFor61Seconds() throws InterruptedException {
        String path = basicCodeGeneration(pesel);
        Thread.sleep(61000);

        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code",  code), Map.class);
        log.info(getHumanReadableResponse(responseEntity));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("RegistrationCodeExpiredException");
    }

}
