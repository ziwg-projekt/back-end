package pl.ziwg.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HospitalCitizenRegisterDto {
    @NotEmpty
    private String city;

    @Email
    private String email;

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @NotEmpty
    private String street;

    @JsonProperty("street_number")
    @NotEmpty
    private String streetNumber;

    @Pattern(regexp = "^\\d{9}$")
    @JsonProperty("phone_number")
    private String phoneNumber;

}
