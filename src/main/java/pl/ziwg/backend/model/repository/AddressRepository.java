package pl.ziwg.backend.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ziwg.backend.model.entity.Address;


@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Page<Address> findAll(Pageable pageable);
}
