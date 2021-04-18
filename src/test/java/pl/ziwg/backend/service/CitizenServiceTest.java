package pl.ziwg.backend.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.ziwg.backend.dto.CitizenUpdateResponseDto;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.enumerates.CitizenState;
import pl.ziwg.backend.model.repository.CitizenRepository;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class CitizenServiceTest {
    @Mock
    private CitizenRepository citizenRepository;

    @Spy
    private CitizenService citizenService;

    @Test
    public void shouldMapFromCitizenToCitizenUpdateResponseDto() {
        //given
        final Citizen citizen = new Citizen();
        final Hospital hospital = new Hospital();
        final Address address = new Address();
        citizen.setName("Jan");
        citizen.setSurname("Kowalski");
        citizen.setPhoneNumber("123456789");
        citizen.setEmail("jankowalski@gmail.com");
        citizen.setState(CitizenState.FINISHED);
        hospital.setName("Jana");
        citizen.setHospital(hospital);
        address.setCitizen(citizen);
        citizen.setAddress(address);
        //when
        final CitizenUpdateResponseDto citizenUpdateResponseDto = citizenService.mapFrom(citizen);
        //then
        assertThat(citizenUpdateResponseDto.getName()).isEqualTo(citizen.getName());
        assertThat(citizenUpdateResponseDto.getSurname()).isEqualTo(citizen.getSurname());
        assertThat(citizenUpdateResponseDto.getPhoneNumber()).isEqualTo(citizen.getPhoneNumber());
        assertThat(citizenUpdateResponseDto.getEmail()).isEqualTo(citizen.getEmail());
        assertThat(citizenUpdateResponseDto.getState()).isEqualTo(citizen.getState());
        assertThat(citizenUpdateResponseDto.getHospital()).isEqualTo(citizen.getHospital());
        assertThat(citizenUpdateResponseDto.getAddress()).isEqualTo(citizen.getAddress());
    }
}
