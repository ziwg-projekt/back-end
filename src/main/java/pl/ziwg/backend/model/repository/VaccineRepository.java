package pl.ziwg.backend.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.model.enumerates.VaccineState;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaccineRepository extends JpaRepository<Vaccine, Long> {
    Page<Vaccine> findAll(Pageable pageable);
    Optional<Vaccine> findByCode(String code);
    List<Vaccine> findByHospitalAndState(Hospital hospital, VaccineState state);
    List<Vaccine> findAllByHospitalAndCompany(Hospital hospital, Company company);
    Boolean existsByCode(String code);
    void deleteByCode(String code);
}
