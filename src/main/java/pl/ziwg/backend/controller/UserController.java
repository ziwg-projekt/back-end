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
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.model.enumerates.UserType;
import pl.ziwg.backend.security.jwt.service.UserPrinciple;
import pl.ziwg.backend.service.AppointmentService;
import pl.ziwg.backend.service.HospitalService;
import pl.ziwg.backend.service.UserService;

import javax.validation.Valid;
import java.util.*;

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
        User user = userService.getUserFromContext();
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
        User user = userService.getUserFromContext();
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(user.getHospital().getVaccines(), Arrays.asList("appointment"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @PostMapping("/self/vaccines")
    public ResponseEntity<List<VaccineDto>> addVaccines(@Valid @RequestBody List<VaccineDto> vaccinesDto){
        User user = userService.getUserFromContext();
        List<VaccineDto> vaccines = appointmentService.createAppointments(user.getHospital(), vaccinesDto);
        return new ResponseEntity<>(vaccines, HttpStatus.OK);
    }


    @PreAuthorize("hasRole('HOSPITAL')")
    @GetMapping("/self/appointments")
    public ResponseEntity<Page<Appointment>> getAppointments(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest,
                                                             @RequestParam(required = false) Optional<Boolean> made,
                                                             @RequestParam(required = false) Optional<Boolean> assigned,
                                                             @RequestParam(required = false) Optional<Boolean> available){
        User user = userService.getUserFromContext();
        Collection<AppointmentState> states = new ArrayList<>(Arrays.asList(AppointmentState.ASSIGNED, AppointmentState.AVAILABLE, AppointmentState.MADE));
        if(made.isPresent()) {
            if (!made.get()) {
                states.remove(AppointmentState.MADE);
            }
        }
        if(assigned.isPresent()) {
            if (!assigned.get()) {
                states.remove(AppointmentState.ASSIGNED);
            }
        }
        if(available.isPresent()) {
            if (!available.get()) {
                states.remove(AppointmentState.AVAILABLE);
            }
        }
        Page<Appointment> appointments = appointmentService.findAllByHospitalAndStateIn(user.getHospital(), states, pageRequest);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }


}
