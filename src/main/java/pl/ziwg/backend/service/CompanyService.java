package pl.ziwg.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.repository.CompanyRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {
    protected static final Logger log = LoggerFactory.getLogger(CompanyService.class);
    private CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository vaccineRepository) {
        this.companyRepository = vaccineRepository;
    }

    public Page<Company> findAllFromPage(Pageable pageable){
        return companyRepository.findAll(pageable);
    }

    public List<Company> findAll(){
        return companyRepository.findAll();
    }

    public Optional<Company> findById(Long id){
        return companyRepository.findById(id);
    }

    public Company findByName(String name){
        Company company = companyRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException(name, "company"));
        return company;
    }

    public Company save(Company vaccine) {
        return companyRepository.save(vaccine);
    }

    public void delete(Company vaccine) {
        companyRepository.delete(vaccine);
    }

    public void addIfNotExists(String company){
        if(companyRepository.existsByName(company)){
            log.info("Company '" + company + "' was found in system");
        }
        else {
            companyRepository.save(new Company(company));
            log.warn("Company '" + company + "' not found in system but has just been added!");
        }
    }


}
