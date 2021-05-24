package pl.ziwg.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.dto.HospitalDto;
import pl.ziwg.backend.exception.UserAlreadyRegisteredException;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.model.repository.HospitalRepository;
import pl.ziwg.backend.model.repository.UserRepository;

import javax.validation.constraints.NotNull;
import java.util.*;

@Service
public class HospitalService {
    protected static final Logger log = LoggerFactory.getLogger(HospitalService.class);

    private HospitalRepository hospitalRepository;
    private CompanyService companyService;
    private VaccineService vaccineService;
    private UserService userService;
    private UserRepository userRepository;

    @Autowired
    public HospitalService(HospitalRepository hospitalRepository,
                           CompanyService companyService,
                           VaccineService vaccineService,
                           UserService userService,
                           UserRepository userRepository) {
        this.hospitalRepository = hospitalRepository;
        this.companyService = companyService;
        this.vaccineService = vaccineService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public Hospital save(@NotNull final HospitalDto newHospital) {
        if (userService.checkIfUserExists(newHospital.getUsername())) {
            log.info("User (type hospital) '" + newHospital.getUsername() + "' was found in system");
            final User user = userRepository.findByUsername(newHospital.getUsername()).get();
            throw new UserAlreadyRegisteredException(String.format("User with username %s already exist",
                    user.getUsername()));
        }

        final Address address = new Address(newHospital.getCity(), newHospital.getStreet(), newHospital.getStreetNumber());
        final Hospital hospital = new Hospital();
        hospital.setAddress(address);
        hospital.setName(newHospital.getName());
        userService.saveHospital(newHospital.getUsername(), newHospital.getPassword(), hospital);
        final User user = userRepository.findByUsername(newHospital.getUsername()).get();
        hospital.setUser(user);
        log.info(String.format("Hospital with name %s was created", hospital.getName()));

        return hospitalRepository.save(hospital);
    }

    public Page<Hospital> findAllFromPage(Pageable pageable){
        return hospitalRepository.findAll(pageable);
    }

    public List<Hospital> findAll(){
        return hospitalRepository.findAll();
    }

    public Optional<Hospital> findById(Long id) {
        return hospitalRepository.findById(id);
    }

    public List<Map<String, Object>> getVaccinesStatistics(Hospital hospital){
        List<Map<String, Object>> statistics = new ArrayList<>();
        List<Company> companies = companyService.findAll();
        for(Company company : companies){
            Map<String, Object> statsPerCompany = new HashMap<>();
            statsPerCompany.put("company", company.getName());
            statsPerCompany.put("logo_path", company.getLogoPath());
            int available = 0;
            int assigned = 0;
            int given = 0;
            List<Vaccine> vaccines = vaccineService.findAllFromGivenCompanyFromHospital(hospital, company);
            for(Vaccine vaccine : vaccines){
                switch(vaccine.getState()){
                    case AVAILABLE:
                    case ASSIGNED_TO_APPOINTMENT:
                        available++;
                        break;
                    case GIVEN:
                        given++;
                        break;
                    case ASSIGNED_TO_CITIZEN:
                        assigned++;
                        break;
                }
            }
            statsPerCompany.put("available", available);
            statsPerCompany.put("assigned", assigned);
            statsPerCompany.put("given", given);
            statistics.add(statsPerCompany);
        }
        return statistics;
    }
}
