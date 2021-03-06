package pl.ziwg.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.model.enumerates.VaccineState;
import pl.ziwg.backend.model.repository.VaccineRepository;

import java.util.List;
import java.util.Optional;

@Service
public class VaccineService {
    private VaccineRepository vaccineRepository;
    protected static final Logger log = LoggerFactory.getLogger(VaccineService.class);

    @Autowired
    public VaccineService(VaccineRepository vaccineRepository) {
        this.vaccineRepository = vaccineRepository;
    }

    public Page<Vaccine> findAllFromPage(Pageable pageable){
        return vaccineRepository.findAll(pageable);
    }

    public List<Vaccine> findAll(){
        return vaccineRepository.findAll();
    }

    public List<Vaccine> findAllFromHospitalByState(Hospital hospital, VaccineState vaccineState){
        return vaccineRepository.findByHospitalAndState(hospital, vaccineState);
    }

    public List<Vaccine> findAllFromGivenCompanyFromHospital(Hospital hospital, Company company){
        return vaccineRepository.findAllByHospitalAndCompany(hospital, company);
    }

    public Optional<Vaccine> findByCode(String code) {
        return vaccineRepository.findByCode(code);
    }

    public Vaccine save(Vaccine vaccine) {
        return vaccineRepository.save(vaccine);
    }

    public Boolean checkIfExistsByCode(String code){
        return vaccineRepository.existsByCode(code);
    }

    public void delete(String code){
        log.info("Deleting vaccine with code " + code + ", total count: " + vaccineRepository.count());
        vaccineRepository.deleteByCode(code);
        log.info("Probably deleted vaccine with code " + code + ", total count: " + vaccineRepository.count());
    }

}
