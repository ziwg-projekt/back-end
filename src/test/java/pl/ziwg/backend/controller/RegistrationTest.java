package pl.ziwg.backend.controller;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class RegistrationTest {
    @LocalServerPort
    private int port;

    private String pesel = "96050834215";

    private String password = "admin12345";

    @Autowired
    private TestRestTemplate restTemplate;

    private String changeString(String stringToChange){
        return stringToChange + "something";
    }

    @Test
    public void goThroughEntireRegistrationProcess() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        path = (String) responseEntity.getBody().get("register_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("password", password), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void invokeIncorrectRegistrationCodeExceptionByChangingCode() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", changeString(code)), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("IncorrectRegistrationCodeException");
    }

    @Test
    public void invokeVerificationTokenDoesNotExistsExceptionByChangingToken() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + changeString(path), Map.of("registration_code", code), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("TokenDoesNotExistsException");
    }


    @Test
    public void invokeRegistrationTokenDoesNotExistsExceptionByChangingToken() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        path = (String) responseEntity.getBody().get("register_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + changeString(path), Map.of("password", password), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("TokenDoesNotExistsException");
    }

    @Test
    public void invokeRegistrationTokenDoesNotExistsExceptionByDoubleConfirm() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        path = (String) responseEntity.getBody().get("register_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("password", password), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("password", password), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("TokenDoesNotExistsException");
    }

    @Test
    public void invokeVerificationAlreadySucceededExceptionByDoubleConfirm() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", password), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("VerificationAlreadySucceededException");
    }

    @Test
    public void invokeIncorrectPayloadSyntaxExceptionByPuttingWrongKeyInRequestBody_v1() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of(changeString("pesel"), pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("IncorrectPayloadSyntaxException");
    }


    @Test
    public void invokeIncorrectPayloadSyntaxExceptionByPuttingWrongKeyInRequestBody_v2() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of(changeString("registration_code"), code), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("IncorrectPayloadSyntaxException");
    }

    @Test
    public void invokeIncorrectPayloadSyntaxExceptionByPuttingWrongKeyInRequestBody_v3() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code", code), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        path = (String) responseEntity.getBody().get("register_api_path");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of(changeString("password"), password), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("IncorrectPayloadSyntaxException");
    }

    @Test
    public void invokeInvalidDataTypeInPayloadExceptionByChangeDataTypeInValueInRequestBody() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", String.valueOf(1)), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("InvalidDataTypeInPayloadException");
    }


    @Test
    public void invokeHttpMessageNotReadableExceptionBySendingEmptyRequestBody() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", "", Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("HttpMediaTypeNotSupportedException");
    }


    @Test
    public void invokeHttpMessageNotReadableExceptionByWrappingCodeInExtraMap() {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");

        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code",  Map.of("registration_code", code)), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("HttpMessageNotReadableException");
    }

    @Disabled
    @Test
    public void invokeRegistrationCodeExpiredExceptionBySleepFor61Seconds() throws InterruptedException {
        ResponseEntity<Map> responseEntity = restTemplate.postForEntity("http://localhost:" + port + "/api/v1/auth/registration/code/generate", Map.of("pesel", pesel,"verification_type", 1), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String path = (String) responseEntity.getBody().get("verify_api_path");
        String code = (String) responseEntity.getBody().get("registration_code");
        Thread.sleep(61000);
        responseEntity = restTemplate.postForEntity("http://localhost:" + port + path, Map.of("registration_code",  code), Map.class);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat((String) responseEntity.getBody().get("exception")).isEqualTo("RegistrationCodeExpiredException");
    }

}
