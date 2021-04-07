package pl.ziwg.backend.externalapi.governmentapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import pl.ziwg.backend.exception.PeselDoesNotExistsException;
import pl.ziwg.backend.exception.UnexpectedResponseFormatException;
import pl.ziwg.backend.property.GovernmentApiProperties;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class PersonRegister {
    Faker faker = new Faker();

    private String apiToken;
    private String urlBase = "http://40.112.78.100:8000/";
    private RestOperations template;
    private ResponseEntity<JsonNode> response;
    private HttpEntity<Map<String, Object>> body;
    private GovernmentApiProperties properties;
    private ObjectMapper jsonObjectMapper = new ObjectMapper();

    @Autowired
    public PersonRegister(GovernmentApiProperties properties){
        this.properties = properties;
        saveApiToken();
    }

    public Person getPersonByPesel(String pesel){
        body = new HttpEntity<>(null, getHeadersWithApiToken());
        response = makeRequest("gov-api/person/" + pesel, body, HttpMethod.GET);
        if(response.getStatusCode().equals(HttpStatus.OK)) {
            try {
                return jsonObjectMapper.treeToValue(response.getBody(), Person.class);
            } catch (JsonProcessingException e) {
                log.error("Unexpected response body, should be convertible to Person, but is - " + response.getBody().toString() + " error is " + e.getMessage());
                throw new PeselDoesNotExistsException("Pesel does not exists!");
            }
        }
        else{
            throw new PeselDoesNotExistsException("Pesel does not exists!");
        }
    }

    public Person getPersonByPeselMock(String pesel){
        return new Person(0, faker.name().firstName(), faker.name().lastName(), pesel, "666999888", "lisradoslaw0@gmail.com");
    }

    private void saveApiToken(){
        template =  new RestTemplate();
        body = new HttpEntity<>(Map.of("username", properties.getUsername(), "password", properties.getPassword()));
        response = makeRequest("api-token-auth/", body, HttpMethod.POST);
        this.apiToken = getParameterFromEntity(response, "token");
    }


    private <T> ResponseEntity<JsonNode> makeRequest(String path, HttpEntity<T> entity, HttpMethod method){
        log.info("Request to " + path + ", method - " + method.toString());
        return template.exchange(urlBase + path, method, entity, JsonNode.class);
    }


    private String getParameterFromEntity(ResponseEntity<JsonNode> entity, String parameter){
        if(Objects.requireNonNull(entity.getBody()).has(parameter)) {
            return entity.getBody().get(parameter).asText();
        }
        else{
            throw new UnexpectedResponseFormatException("Parameter '" + parameter + "' is missing!");
        }
    }

    private HttpHeaders getHeadersWithApiToken(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Token " + this.apiToken);
        return headers;
    }

}
