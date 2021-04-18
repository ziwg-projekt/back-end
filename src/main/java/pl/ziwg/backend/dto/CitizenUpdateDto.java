package pl.ziwg.backend.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.enumerates.CitizenState;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CitizenUpdateDto {
    private String name;

    private String surname;

    @Pattern(regexp = "^\\d{9}$")
    @ApiModelProperty(example = "123456789")
    private String phoneNumber;

    @Email
    private String email;

    private Address address;

    private Hospital hospital;

    private CitizenState state;
}
