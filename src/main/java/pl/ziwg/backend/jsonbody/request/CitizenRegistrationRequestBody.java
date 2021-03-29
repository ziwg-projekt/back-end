package pl.ziwg.backend.jsonbody.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CitizenRegistrationRequestBody {
    @NotEmpty
    private String password;

    @NotEmpty
    private String username;

    @NotEmpty
    private String city;

    @NotEmpty
    private String street;

    @JsonProperty("street_number")
    @NotEmpty
    private String streetNumber;

}
