package pl.ziwg.backend.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.dto.VaccineDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.model.enumerates.UserType;
import pl.ziwg.backend.security.jwt.service.UserPrinciple;
import pl.ziwg.backend.service.AppointmentService;
import pl.ziwg.backend.service.HospitalService;
import pl.ziwg.backend.service.UserService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private UserService userService;
    private HospitalService hospitalService;
    private AppointmentService appointmentService;

    public UserController(UserService userService, HospitalService hospitalService, AppointmentService appointmentService){
        this.userService = userService;
        this.hospitalService = hospitalService;
        this.appointmentService = appointmentService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    public ResponseEntity<Company> deleteByUsername(@PathVariable String username) {
        userService.deleteUser(username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user = userService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "user"));
        return new ResponseEntity<>(user, HttpStatus.OK);
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


    @PreAuthorize("hasRole('HOSPITAL')")
    @PostMapping("/self/vaccines")
    public ResponseEntity<List<VaccineDto>> addVaccines(@Valid @RequestBody List<VaccineDto> vaccinesDto){
        User user = getUserFromContext();
        List<VaccineDto> vaccines = appointmentService.createAppointments(user.getHospital(), vaccinesDto);
        return new ResponseEntity<>(vaccines, HttpStatus.OK);
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

    @PreAuthorize("hasRole('HOSPITAL')")
    @GetMapping("/self/appointments")
    public ResponseEntity<Page<Appointment>> getAppointments(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest){
        User user = getUserFromContext();
        Page<Appointment> appointments = appointmentService.findAllFromHospitalByPage(user.getHospital(), pageRequest);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }


}
