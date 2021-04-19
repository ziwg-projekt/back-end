package pl.ziwg.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AddressUpdateDto {
    @NotEmpty
    private String city;

    @NotEmpty
    private String street;

    @JsonProperty(value="street_number")
    @NotEmpty
    private String streetNumber;
}
