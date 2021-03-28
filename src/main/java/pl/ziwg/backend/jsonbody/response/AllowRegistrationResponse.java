package pl.ziwg.backend.jsonbody.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import pl.ziwg.backend.externalapi.governmentapi.Person;

@Getter
@Setter
@ToString
public class AllowRegistrationResponse {
    @JsonProperty("register_api_path")
    private String registerApiPath;
    private Person person;

    public AllowRegistrationResponse(String token, Person person){
        this.registerApiPath = "/api/v1/auth/registration/" + token;
        this.person = person;

    }
}
