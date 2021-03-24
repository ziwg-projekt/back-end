package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.repository.CompanyRepository;

import java.util.List;
import java.util.Optional;

@Service
public class CompanyService {
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

    public Company save(Company vaccine) {
        return companyRepository.save(vaccine);
    }

    public void delete(Company vaccine) {
        companyRepository.delete(vaccine);
    }


}
