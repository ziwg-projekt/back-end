package pl.ziwg.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.model.enumerates.UserType;
import pl.ziwg.backend.security.jwt.service.UserPrinciple;
import pl.ziwg.backend.service.HospitalService;
import pl.ziwg.backend.service.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private UserService userService;
    private HospitalService hospitalService;

    public UserController(UserService userService, HospitalService hospitalService){
        this.userService = userService;
        this.hospitalService = hospitalService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<Company> deleteByUsername(@PathVariable String username) {
        userService.deleteUser(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('CITIZEN') || hasRole('HOSPITAL')")
    @GetMapping("/self/address")
    public ResponseEntity<Address> getUserAddress(){
        User user = getUserFromContext();
        if(user.getUserType().equals(UserType.CITIZEN)){
            return new ResponseEntity<>(user.getCitizen().getAddress(), HttpStatus.OK);
        }
        else{
            return new ResponseEntity<>(user.getHospital().getAddress(), HttpStatus.OK);
        }
    }

    @PreAuthorize("hasRole('HOSPITAL')")
    @GetMapping("/self/vaccines")
    public ResponseEntity<List<Map<String, Object>>> getVaccines(){
        User user = getUserFromContext();
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(user.getHospital().getVaccines(), Arrays.asList("appointment"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private User getUserFromContext(){
        UserPrinciple up = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Optional<User> optionalUser = userService.findById(up.getId());
        if(optionalUser.isPresent()){
            return optionalUser.get();
        }
        else{
            throw new ResourceNotFoundException("id", "user");
        }
    }
}
