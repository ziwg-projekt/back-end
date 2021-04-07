package pl.ziwg.backend.controller;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.http.*;
import pl.ziwg.backend.exception.UnexpectedResponseFormatException;
import pl.ziwg.backend.service.AuthenticationService;
import pl.ziwg.backend.service.UserService;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthenticationTest {
    @LocalServerPort
    private int port;

    private AuthenticationService authenticationService;
    private UserService userService;
    private TestRestTemplate restTemplate;

    @Autowired
    public AuthenticationTest(AuthenticationService authenticationService, UserService userService, TestRestTemplate restTemplate){
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.restTemplate = restTemplate;
    }

    protected static final Logger log = Logger.getLogger(AuthenticationService.class);

    private String pesel = "94040743567";
    private String pesel_2 = "81071149213";
    private String code = "123456";
    private String citizenPassword = "password12345";
    private String citizenUsername = "user_test";
    private String hospitalUsername = "szpitalicho";
    private String hospitalPassword = "hospitalpass";
    private final String adminPassword = "adminpassword";
    private final String adminUsername = "admin";
    private ResponseEntity<JsonNode> response;
    private HttpEntity<Map<String, Object>> body;

    @BeforeEach
    public void procedureBefore(){
        log.info("STARTING NEW TEST CASE");
        authenticationService.showCurrentState();
    }

    @AfterEach
    public void procedureAfter(){
        try{
            userService.deleteUser(citizenUsername);
            userService.deleteUser(hospitalUsername);
        } catch(Exception ex){
            log.info(ex.getMessage());
        }
        log.info("ENDING TEST CASE");
        authenticationService.showCurrentState();
    }

    @Test
    public void failLogin(){
        response = login(citizenUsername, citizenPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("BadCredentialsException");
    }

    @Test
    public void passLogin(){
        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), code);
        response = basicRegistration(getRegisterApiPath(response), citizenPassword, citizenUsername);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = login(citizenUsername, citizenPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void adminLogin(){
        response = login(adminUsername, adminPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void registerHospitalByAdmin(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        response = login(adminUsername, adminPassword);

        headers.set("Authorization", "Bearer " + getParameterFromEntity(response, "access_token"));
        Map<String, Object> requestBody = getHospitalInformation(hospitalUsername, hospitalPassword);
        body = new HttpEntity<>(requestBody, headers);
        response = makePostRequest("/api/v1/auth/registration/hospital/register", body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void invokeForbiddenOnRegisterHospitalByCitizen(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), code);
        response = basicRegistration(getRegisterApiPath(response), citizenPassword, citizenUsername);

        response = login(citizenUsername, citizenPassword);

        headers.set("Authorization", "Bearer " + getParameterFromEntity(response, "access_token"));
        Map<String, Object> requestBody = getHospitalInformation(hospitalUsername, hospitalPassword);
        body = new HttpEntity<>(requestBody, headers);
        response = makePostRequest("/api/v1/auth/registration/hospital/register", body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void invokeUnauthorizedOnRegisterHospitalAnonymously(){
        Map<String, Object> requestBody = getHospitalInformation(hospitalUsername, hospitalPassword);
        body = new HttpEntity<>(requestBody);
        response = makePostRequest("/api/v1/auth/registration/hospital/register", body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void checkIfAddressIsAccessibleFromContext(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), code);
        response = basicRegistration(getRegisterApiPath(response), citizenPassword, citizenUsername);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = login(citizenUsername, citizenPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        headers.set("Authorization", "Bearer " + getParameterFromEntity(response, "access_token"));
        body = new HttpEntity<>(headers);
        response = makeGetRequest("/api/v1/users/self/address", body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void checkIfVaccinesAreAccessibleFromHospitalContext(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        response = login(adminUsername, adminPassword);

        headers.set("Authorization", "Bearer " + getParameterFromEntity(response, "access_token"));
        Map<String, Object> requestBody = getHospitalInformation(hospitalUsername, hospitalPassword);
        body = new HttpEntity<>(requestBody, headers);
        response = makePostRequest("/api/v1/auth/registration/hospital/register", body);

        response = login(hospitalUsername, hospitalPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        headers.set("Authorization", "Bearer " + getParameterFromEntity(response, "access_token"));
        body = new HttpEntity<>(headers);
        response = makeGetRequest("/api/v1/users/self/vaccines", body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void checkIfVaccinesAreAccessibleFromCitizenContext(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), code);
        response = basicRegistration(getRegisterApiPath(response), citizenPassword, citizenUsername);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = login(citizenUsername, citizenPassword);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        headers.set("Authorization", "Bearer " + getParameterFromEntity(response, "access_token"));
        body = new HttpEntity<>(headers);
        response = makeGetRequest("/api/v1/users/self/vaccines", body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void checkIfAllRolesPresent(){
        assertThat(authenticationService.checkIfAllRolesPresent()).isEqualTo(true);
    }

    @Test
    public void goThroughEntireRegistrationProcess() {
        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), code);
        response = basicRegistration(getRegisterApiPath(response), citizenPassword, citizenUsername);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void invokeIncorrectRegistrationCodeExceptionByChangingCode() {
        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), changeString(code));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("IncorrectRegistrationCodeException");
    }

    @Test
    public void invokeUserAlreadyRegisteredException(){
        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), code);
        response = basicRegistration(getRegisterApiPath(response), citizenPassword, citizenUsername);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        response = basicCodeGeneration(pesel, 1);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("UserAlreadyRegisteredException");
    }

    @Test
    public void invokeUsernameNotAvailableException(){
        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), code);
        response = basicRegistration(getRegisterApiPath(response), citizenPassword, citizenUsername);

        response = basicCodeGeneration(pesel_2, 1);
        response = basicVerification(getVerifyApiPath(response), code);

        response = basicRegistration(getRegisterApiPath(response), citizenPassword, citizenUsername);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("UsernameNotAvailableException");
    }

    @Test
    public void invokeVerificationTokenDoesNotExistsExceptionByChangingToken() {
        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response) + 'x', code);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("TokenDoesNotExistsException");
    }


    @Test
    public void invokeRegistrationTokenDoesNotExistsExceptionByChangingToken() {
        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), code);
        response = basicRegistration(getRegisterApiPath(response) + 'x', citizenPassword, citizenUsername);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("TokenDoesNotExistsException");
    }

    @Test
    public void invokeRegistrationTokenDoesNotExistsExceptionByDoubleConfirm() {
        response = basicCodeGeneration(pesel, 1);
        response = basicVerification(getVerifyApiPath(response), code);
        String registerApiPath = getRegisterApiPath(response);
        response = basicRegistration(registerApiPath, citizenPassword, citizenUsername);
        response = basicRegistration(registerApiPath, citizenPassword, citizenUsername);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("TokenDoesNotExistsException");
    }

    @Test
    public void invokeVerificationAlreadySucceededExceptionByDoubleConfirm() {
        response = basicCodeGeneration(pesel, 1);
        String verifyApiPath = getVerifyApiPath(response);
        response = basicVerification(verifyApiPath, code);
        response = basicVerification(verifyApiPath, code);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("VerificationAlreadySucceededException");
    }

    @Test
    public void invokeMethodArgumentNotValidExceptionByPuttingWrongKeyInRequestBody_v1() {
        body = new HttpEntity<>(Map.of(changeString("pesel"), pesel,"communication_channel_type", 1));
        response = makePostRequest("/api/v1/auth/registration/citizen/notify", body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("MethodArgumentNotValidException");
    }

    @Test
    public void invokeHttpMessageNotReadableExceptionBySendingEmptyRequestBody() {
        HttpEntity<String> body = new HttpEntity<>("");
        response = makePostRequest("/api/v1/auth/registration/citizen/notify", body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("HttpMediaTypeNotSupportedException");
    }


    @Test
    public void invokeHttpMessageNotReadableExceptionByWrappingCodeInExtraMap() {
        response = basicCodeGeneration(pesel, 1);
        HttpEntity<Map<String, Object>> body = new HttpEntity<>(Map.of("registration_code",  Map.of("registration_code", code)));
        response = makePostRequest("/api/v1/auth/registration/citizen/verify", body);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("HttpMessageNotReadableException");
    }

    @Disabled  // enable if you want to add extra 61 seconds to tests execution time
    @Test
    public void invokeRegistrationCodeExpiredExceptionBySleepFor61Seconds() throws InterruptedException {
        response = basicCodeGeneration(pesel, 1);
        Thread.sleep(61000);

        response = basicVerification(getVerifyApiPath(response), code);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(getParameterFromEntity(response, "exception")).isEqualTo("RegistrationCodeExpiredException");
    }


    private String getParameterFromEntity(ResponseEntity<JsonNode> entity, String parameter){
        if(Objects.requireNonNull(entity.getBody()).has(parameter)) {
            return entity.getBody().get(parameter).asText();
        }
        else{
            throw new UnexpectedResponseFormatException("Parameter '" + parameter + "' is missing!");
        }
    }

    private <T> ResponseEntity<JsonNode> makePostRequest(String path, HttpEntity<T> entity){
        ResponseEntity<JsonNode> response = restTemplate.exchange("http://localhost:" + port + path, HttpMethod.POST, entity, JsonNode.class);
        log.info(getHumanReadableResponse(response));
        return response;
    }

    private <T> ResponseEntity<JsonNode> makeGetRequest(String path, HttpEntity<T> entity){
        ResponseEntity<JsonNode> response = restTemplate.exchange("http://localhost:" + port + path, HttpMethod.GET, entity, JsonNode.class);
        log.info(getHumanReadableResponse(response));
        return response;
    }

    private ResponseEntity<JsonNode> basicCodeGeneration(String pesel, int type){
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("pesel", pesel,"communication_channel_type", type));
        return makePostRequest("/api/v1/auth/registration/citizen/notify", entity);
    }

    private ResponseEntity<JsonNode> basicVerification(String path, String code){
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>( Map.of("registration_code", code));
        return makePostRequest(path, entity);
    }

    private ResponseEntity<JsonNode> basicRegistration(String path, String password, String username){
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(Map.of("password", password,
                                                                        "username", username,
                                                                        "street_number", "47",
                                                                        "city", "Wroclaw",
                                                                        "street", "Zielinskiego"));
        return makePostRequest(path, entity);
    }

    private String getVerifyApiPath(ResponseEntity<JsonNode> entity){
        return getParameterFromEntity(entity, "verify_api_path");
    }

    private String getRegisterApiPath(ResponseEntity<JsonNode> entity){
        return getParameterFromEntity(entity, "register_api_path");
    }

    private ResponseEntity<JsonNode> login(String username, String password){
        body = new HttpEntity<>(Map.of("password", password, "username", username));
        return makePostRequest("/api/v1/auth/login", body);
    }

    private Map<String, Object> getHospitalInformation(String username, String password){
        return Map.of("password", password,
                "username", username,
                "hospital_name", "szpitalisko we wroclawiu",
                "city", "Wroclaw",
                "street", "Grunwaldzka",
                "street_number", "12c");
    }

    private String changeString(String stringToChange){
        int index = stringToChange.length() / 2;
        return stringToChange.substring(0, index) + 'x' + stringToChange.substring(index + 1);
    }

    private String getHumanReadableResponse(ResponseEntity<JsonNode> responseEntity){
        return "Status code = " + responseEntity.getStatusCode() + ", body = " + responseEntity.getBody();
    }


}
