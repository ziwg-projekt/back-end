package pl.ziwg.backend.controller;

import org.apache.log4j.Logger;
import org.aspectj.lang.annotation.Before;
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
public class RegistrationTest {
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
        String newString = stringToChange.substring(0, index) + 'x' + stringToChange.substring(index + 1);
        return newString;
    }

    @BeforeEach
    public void showCurrentStateBefore(){
        log.info("STARTING NEW TEST CASE");
        authenticationService.showCurrentState();
    }

    @AfterEach
    public void showCurrentStateAfter(){
        log.info("ENDING TEST CASE");
        authenticationService.showCurrentState();
    }

    @Test
    public void goThroughEntireRegistrationProcess() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        path = (String) responseEntity.getBody().get("register_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("password", password, "username", username), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        authenticationService.deleteUser(username);
        authenticationService.deleteCitizen(pesel);
    }

    @Test
    public void invokeIncorrectRegistrationCodeExceptionByChangingCode() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", changeString(code)), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("IncorrectRegistrationCodeException");
    }

    @Test
    public void invokeVerificationTokenDoesNotExistsExceptionByChangingToken() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path + "x", Map.of("registration_code", code), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("TokenDoesNotExistsException");
    }


    @Test
    public void invokeRegistrationTokenDoesNotExistsExceptionByChangingToken() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        path = (String) responseEntity.getBody().get("register_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path + "x", Map.of("password", password, "username", username), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("TokenDoesNotExistsException");
    }

    @Test
    public void invokeRegistrationTokenDoesNotExistsExceptionByDoubleConfirm() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        path = (String) responseEntity.getBody().get("register_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("password", password, "username", username), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("password", password, "username", username), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("TokenDoesNotExistsException");

        authenticationService.deleteUser(username);
        authenticationService.deleteCitizen(pesel);
    }

    @Test
    public void invokeVerificationAlreadySucceededExceptionByDoubleConfirm() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", password), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("VerificationAlreadySucceededException");

    }

    @Test
    public void invokeMethodArgumentNotValidExceptionByPuttingWrongKeyInRequestBody_v1() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of(changeString("pesel"), pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("MethodArgumentNotValidException");

    }

    @Test
    public void invokeMethodArgumentNotValidExceptionByPuttingWrongKeyInRequestBody_v2() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of(changeString("registration_code"), code), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("MethodArgumentNotValidException");
    }

    @Test
    public void invokeMethodArgumentNotValidExceptionByPuttingWrongKeyInRequestBody_v3() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        path = (String) responseEntity.getBody().get("register_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of(changeString("password"), password, "username", username), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("MethodArgumentNotValidException");
    }

    @Test
    public void invokeHttpMessageNotReadableExceptionBySendingEmptyRequestBody() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", "", Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("HttpMediaTypeNotSupportedException");
    }


    @Test
    public void invokeHttpMessageNotReadableExceptionByWrappingCodeInExtraMap() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code",  Map.of("registration_code", code)), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("HttpMessageNotReadableException");
    }

    @Disabled
    @Test
    public void invokeRegistrationCodeExpiredExceptionBySleepFor61Seconds() throws InterruptedException {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"communication_channel_type", 1), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        Thread.sleep(61000);
        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code",  code), Map.class);
        log.info(responseEntity.toString());
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("RegistrationCodeExpiredException");
    }

}
