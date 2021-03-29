package pl.ziwg.backend.jsonbody.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.ziwg.backend.model.entity.Hospital;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HospitalRegistrationRequestBody {
    @NotEmpty
    private String password;

    @NotEmpty
    private String username;

    @JsonProperty("hospital_name")
    @NotEmpty
    private String hospitalName;

    @NotEmpty
    private String city;

    @NotEmpty
    private String street;

    @JsonProperty("street_number")
    @NotEmpty
    private String streetNumber;

}
