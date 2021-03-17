package pl.ziwg.backend.service.entityaccess;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.repository.HospitalRepository;

import java.util.Optional;

@Service
public class HospitalAccessService {

    private final HospitalRepository hospitalRepository;

    @Autowired
    public HospitalAccessService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    public Iterable<Hospital> findAll() {
        return hospitalRepository.findAll();
    }

    public Optional<Hospital> findById(Long id) {
        return hospitalRepository.findById(id);
    }
}
