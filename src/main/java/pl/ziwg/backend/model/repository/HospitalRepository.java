package pl.ziwg.backend.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ziwg.backend.model.entity.Hospital;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

}
