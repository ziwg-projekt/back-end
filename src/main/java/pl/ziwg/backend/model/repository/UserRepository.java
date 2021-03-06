package pl.ziwg.backend.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ziwg.backend.model.entity.Role;
import pl.ziwg.backend.model.entity.User;

import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    void deleteByUsername(String username);
    Optional<User> findByRolesIn(Set<Role> roles);
    boolean existsByRolesIn(Set<Role> roles);
}
