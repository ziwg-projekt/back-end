package pl.ziwg.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.enumerates.CitizenState;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CitizenUpdateDto {
    private String name;

    private String surname;

    private String phoneNumber;

    private String email;

    private Address address;

    private Hospital hospital;

    private CitizenState state;
}
