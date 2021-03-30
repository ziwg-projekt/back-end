package pl.ziwg.backend.service;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.model.entity.Role;
import pl.ziwg.backend.model.entity.RoleName;
import pl.ziwg.backend.model.repository.RoleRepository;

import java.util.Optional;

@Service
public class RoleService {
    protected static final Logger log = Logger.getLogger(BackendApplication.class);
    private RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    public void addIfNotExists(RoleName roleName){
        Optional<Role> role = roleRepository.findByName(roleName);
        if(role.isEmpty()){
            roleRepository.save(new Role(roleName));
            log.info("Role " + roleName.toString() +" not found but has just been added!");
        }
        else{
            log.info("Role " + roleName.toString() + " was found");
        }
    }

    public Optional<Role> findByName(RoleName roleName){
        return roleRepository.findByName(roleName);
    }
}
