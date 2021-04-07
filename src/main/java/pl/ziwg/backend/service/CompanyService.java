package pl.ziwg.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.repository.CompanyRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {
    protected static final Logger log = Logger.getLogger(BackendApplication.class);
    private CompanyRepository companyRepository;

    @Autowired
    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
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

    public Company save(Company company) {
        return companyRepository.save(company);
    }

    public void delete(Company company) {
        companyRepository.delete(company);
    }

    public Company addIfNotExists(String companyName){
        Optional<Company> company = companyRepository.findByName(companyName);
        if(company.isPresent()){
            log.info("Company '" + companyName + "' was found in system");
            return company.get();
        }
        else {
            log.warn("Company '" + companyName + "' not found in system but has just been added!");
            return save(new Company(companyName));
        }
    }


}
