package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.model.repository.HospitalRepository;

import java.util.*;

@Service
public class HospitalService {
    private HospitalRepository hospitalRepository;
    private CompanyService companyService;
    private VaccineService vaccineService;

    @Autowired
    public HospitalService(HospitalRepository hospitalRepository,
                           CompanyService companyService,
                           VaccineService vaccineService) {
        this.hospitalRepository = hospitalRepository;
        this.companyService = companyService;
        this.vaccineService = vaccineService;
    }


    public Hospital save(Hospital hospital) {
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
