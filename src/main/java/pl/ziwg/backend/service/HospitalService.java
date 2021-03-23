package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.repository.HospitalRepository;

import java.util.List;
import java.util.Optional;

@Service
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    @Autowired
    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
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
}
