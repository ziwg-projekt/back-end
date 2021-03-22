package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.model.repository.VaccineRepository;

import java.util.List;
import java.util.Optional;

@Service
public class VaccineService {
    private VaccineRepository vaccineRepository;

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


    public Optional<Vaccine> findByCode(String code) {
        return vaccineRepository.findByCode(code);
    }

    public Vaccine save(Vaccine vaccine) {
        return vaccineRepository.save(vaccine);
    }

}
