package pl.ziwg.backend.jsonbody.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RegistrationCodeRequestBody {
    @JsonProperty("registration_code")
    @NotNull
    private String registrationCode;
}
