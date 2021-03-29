package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.model.repository.DoctorRepository;
import pl.ziwg.backend.model.repository.HospitalRepository;
import pl.ziwg.backend.model.repository.UserRepository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Page<User> findAllFromPage(Pageable pageable){
        return userRepository.findAll(pageable);
    }

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(String username){
        userRepository.deleteByUsername(username);
    }

}
