package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.model.repository.VaccineRepository;

import java.util.Optional;

@Service
public class VaccineService {
    private VaccineRepository vaccineRepository;

    @Autowired
    public VaccineService(VaccineRepository vaccineRepository) {
        this.vaccineRepository = vaccineRepository;
    }

    public Page<Vaccine> findAll(Pageable pageable){
        return vaccineRepository.findAll(pageable);
    }

    public Optional<Vaccine> findById(Long id){
        return vaccineRepository.findById(id);
    }

    public Vaccine save(Vaccine vaccine) {
        return vaccineRepository.save(vaccine);
    }
}
