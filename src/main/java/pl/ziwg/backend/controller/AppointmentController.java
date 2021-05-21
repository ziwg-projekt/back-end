package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.dto.AppointmentDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.security.jwt.service.UserPrinciple;
import pl.ziwg.backend.service.AppointmentService;
import pl.ziwg.backend.service.UserService;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private AppointmentService appointmentService;
    private UserService userService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, UserService userService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Appointment>> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
        return new ResponseEntity<>(appointmentService.findAllFromPage(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getOne(@PathVariable Long id) {
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "appointment"));
        return new ResponseEntity<>(appointment, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PatchMapping("/{id}/actions/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable Long id) {
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "appointment"));
        appointment.setState(AppointmentState.AVAILABLE);
        appointmentService.save(appointment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PatchMapping("/{id}/actions/enroll")
    public ResponseEntity<Appointment> enroll(@PathVariable Long id) {
        User user = getUserFromContext();
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "appointment"));

        appointment.setState(AppointmentState.ASSIGNED);
        appointment.setCitizen(user.getCitizen());
        appointmentService.save(appointment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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


