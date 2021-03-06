package pl.ziwg.backend.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ziwg.backend.model.entity.Citizen;

import java.util.Optional;

@Repository
public interface CitizenRepository extends JpaRepository<Citizen, Long> {
    Page<Citizen> findAll(Pageable pageable);
    Optional<Citizen> findByPesel(String pesel);
    Boolean existsByPesel(String pesel);
    void deleteByPesel(String pesel);
}
