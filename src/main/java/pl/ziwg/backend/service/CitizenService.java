package pl.ziwg.backend.service;

import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.pl.PESEL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.dto.CitizenUpdateDto;
import pl.ziwg.backend.dto.CitizenUpdateResponseDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.externalapi.governmentapi.Person;
import pl.ziwg.backend.externalapi.governmentapi.PersonRegister;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.enumerates.CitizenState;
import pl.ziwg.backend.model.repository.CitizenRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Transactional
@Service
@NoArgsConstructor
public class CitizenService {
    private CitizenRepository citizenRepository;
    private HospitalService hospitalService;
    private PersonRegister personRegister;

    @Autowired
    public CitizenService(CitizenRepository citizenRepository, HospitalService hospitalService, PersonRegister personRegister) {
        this.citizenRepository = citizenRepository;
        this.hospitalService = hospitalService;
        this.personRegister = personRegister;
    }

    public Page<Citizen> findAllFromPage(Pageable pageable) {
        return citizenRepository.findAll(pageable);
    }

    public List<Citizen> findAll() {
        return citizenRepository.findAll();
    }

    public Optional<Citizen> findByPesel(String pesel) {
        return citizenRepository.findByPesel(pesel);
    }

    public Citizen save(Citizen citizen) {
        return citizenRepository.save(citizen);
    }

    public void deleteCitizen(String pesel) {
        citizenRepository.deleteByPesel(pesel);
    }

    public boolean checkIfExistsByPesel(String pesel) {
        return citizenRepository.existsByPesel(pesel);
    }

    public Person getPersonByPeselFromGovernmentApi(String pesel){
        return personRegister.getPersonByPesel(pesel);
    }

    public long getAmountOfVaccinatedCitizens(){
        return findAll().stream().filter(e -> e.getState().equals(CitizenState.FINISHED)).count();
    }

    public CitizenUpdateResponseDto updateCitizenData(final CitizenUpdateDto citizenUpdateDto,
                                                      @PESEL final String pesel) {
        final Citizen citizen = citizenRepository.findByPesel(pesel)
                .orElseThrow(() -> new ResourceNotFoundException(pesel, "citizen"));

        if (Objects.nonNull(citizenUpdateDto.getName())) {
            citizen.setName(citizenUpdateDto.getName());
        }
        if (Objects.nonNull(citizenUpdateDto.getSurname())) {
            citizen.setSurname(citizenUpdateDto.getSurname());
        }
        if (Objects.nonNull(citizenUpdateDto.getAddress())) {
            final Address address = new Address(citizenUpdateDto.getAddress().getCity(),
                    citizenUpdateDto.getAddress().getStreet(), citizenUpdateDto.getAddress().getStreetNumber());
            citizen.setAddress(address);
        }
        if (Objects.nonNull(citizenUpdateDto.getEmail())) {
            citizen.setEmail(citizenUpdateDto.getEmail());
        }
        if (Objects.nonNull(citizenUpdateDto.getPhoneNumber())) {
            citizen.setPhoneNumber(citizenUpdateDto.getPhoneNumber());
        }
        if (Objects.nonNull(citizenUpdateDto.getState())) {
            citizen.setState(citizenUpdateDto.getState());
        }
        if (Objects.nonNull(citizenUpdateDto.getHospitalId())) {
            final Hospital hospital = hospitalService.findById(citizenUpdateDto.getHospitalId())
                    .orElseThrow(() -> new ResourceNotFoundException(citizenUpdateDto.getHospitalId(), "hospitalId"));
            citizen.setHospital(hospital);
        }
        citizenRepository.save(citizen);
        return mapFrom(citizen);
    }

    public CitizenUpdateResponseDto mapFrom(final Citizen citizen) {
        final CitizenUpdateResponseDto citizenUpdateResponseDto = new CitizenUpdateResponseDto();
        citizenUpdateResponseDto.setName(citizen.getName());
        citizenUpdateResponseDto.setSurname(citizen.getSurname());
        citizenUpdateResponseDto.setEmail(citizen.getEmail());
        citizenUpdateResponseDto.setAddress(citizen.getAddress());
        citizenUpdateResponseDto.setHospital(citizen.getHospital());
        citizenUpdateResponseDto.setPhoneNumber(citizen.getPhoneNumber());
        citizenUpdateResponseDto.setState(citizen.getState());
        citizenUpdateResponseDto.setPesel(citizen.getPesel());

        return citizenUpdateResponseDto;
    }

}
