package pl.ziwg.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.externalapi.governmentapi.Person;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.model.enumerates.UserType;
import pl.ziwg.backend.model.repository.DoctorRepository;
import pl.ziwg.backend.model.repository.HospitalRepository;
import pl.ziwg.backend.model.repository.RoleRepository;
import pl.ziwg.backend.model.repository.UserRepository;
import pl.ziwg.backend.security.jwt.service.UserPrinciple;

import javax.transaction.Transactional;
import java.util.*;

@Transactional
@Service
public class UserService {
    protected static final Logger log = LoggerFactory.getLogger(UserService.class);

    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder encoder;


    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
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

    public boolean checkIfExistsUserWithGivenAuthorities(List<Role> roles){
        return userRepository.existsByRolesIn(new HashSet<>(roles));
    }

    public boolean checkIfUserExists(String username){
        return userRepository.existsByUsername(username);
    }

    public void deleteUser(String username){
        userRepository.deleteByUsername(username);
    }

    public void saveCitizen(String username, String password, Citizen citizen){
        User user = new User(username, encoder.encode(password), citizen);
        user.setRoles(new HashSet<>(Collections.singletonList(roleRepository.findByName(RoleName.ROLE_CITIZEN).get())));
        saveUser(user);
        log.info("User (type citizen) '" + username + "' has been added!");
    }

    public void saveHospital(String username, String password, Hospital hospital){
        User user = new User(username, encoder.encode(password), hospital);
        user.setRoles(new HashSet<>(Collections.singletonList(roleRepository.findByName(RoleName.ROLE_HOSPITAL).get())));
        saveUser(user);
        log.info("User (type hospital) '" + username + "' has been added!");
    }

    public void saveAdmin(String username, String password){
        List<Role> adminRoles = Arrays.asList(roleRepository.findByName(RoleName.ROLE_ADMIN).get(), roleRepository.findByName(RoleName.ROLE_CITIZEN).get(), roleRepository.findByName(RoleName.ROLE_HOSPITAL).get());
        User user = new User(username, encoder.encode(password), UserType.ADMIN);
        user.setRoles(new HashSet<>(adminRoles));
        saveUser(user);
        log.info("User (type admin) '" + username + "' has been added!");
    }

    private void saveUser(User user){
        userRepository.save(user);
    }

    public User getUserFromContext(){
        UserPrinciple up = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Optional<User> optionalUser = findById(up.getId());
        if(optionalUser.isPresent()){
            return optionalUser.get();
        }
        else{
            throw new ResourceNotFoundException("id", "user");
        }
    }


}
