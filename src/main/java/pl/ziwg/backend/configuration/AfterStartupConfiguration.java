package pl.ziwg.backend.configuration;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.model.entity.Role;
import pl.ziwg.backend.model.entity.RoleName;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.model.enumerates.UserType;
import pl.ziwg.backend.model.repository.RoleRepository;
import pl.ziwg.backend.model.repository.UserRepository;

import java.util.*;

@Component
public class AfterStartupConfiguration {
    protected static final Logger log = Logger.getLogger(BackendApplication.class);
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder encoder;
    private final String password = "adminpassword";
    private final String username = "admin";

    @Autowired
    public AfterStartupConfiguration(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder encoder){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.encoder = encoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createAdminIfNotExists() {
        List<Role> adminRoles = Arrays.asList(roleRepository.findByName(RoleName.ROLE_ADMIN).get(), roleRepository.findByName(RoleName.ROLE_CITIZEN).get(), roleRepository.findByName(RoleName.ROLE_HOSPITAL).get());
        log.info("Looking for admin in User table");
        Optional<User> optionalAdmin = userRepository.findByRolesIn(new HashSet<>(Collections.singletonList(roleRepository.findByName(RoleName.ROLE_ADMIN).get())));
        if(optionalAdmin.isEmpty()) {
            User user = new User(username, encoder.encode(password), UserType.ADMIN);
            user.setRoles(new HashSet<>(adminRoles));
            this.userRepository.save(user);
            log.info("Admin not found but has just been added!");
        }
        else{
            log.info("Admin was found - " + optionalAdmin.get().toString());
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createRolesIfNotExist(){
        log.info("Looking for roles in Role table");
        for (RoleName roleName : RoleName.values()) {
            Optional<Role> role = roleRepository.findByName(roleName);
            if(role.isEmpty()){
                roleRepository.save(new Role(roleName));
                log.info("Role " + roleName.toString() +" not found but has just been added!");
            }
            else{
                log.info("Role " + roleName.toString() + " was found");
            }
        }

    }
}
