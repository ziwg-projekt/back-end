package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ziwg.backend.dto.HospitalEnrollDto;
import pl.ziwg.backend.dto.VaccineDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.exception.UserTypeException;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.model.enumerates.UserType;
import pl.ziwg.backend.model.enumerates.VaccineState;
import pl.ziwg.backend.notificator.email.EmailSubject;
import pl.ziwg.backend.service.AppointmentService;
import pl.ziwg.backend.service.CitizenService;
import pl.ziwg.backend.service.EmailService;
import pl.ziwg.backend.service.UserService;

import java.time.LocalDateTime;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private AppointmentService appointmentService;
    private CitizenService citizenService;
    private UserService userService;
    private EmailService emailService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, CitizenService citizenService, UserService userService, EmailService emailService) {
        this.appointmentService = appointmentService;
        this.citizenService = citizenService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Appointment>> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
        return new ResponseEntity<>(appointmentService.findAllFromPage(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getOne(@PathVariable Long id) {
        return appointmentService.getAppointmentById(id);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PatchMapping("/{id}/actions/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable Long id) {
        return appointmentService.cancelAppointmentByCitizen(id);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PatchMapping("/{id}/actions/enroll")
    public ResponseEntity<Appointment> enroll(@PathVariable Long id) {
        return appointmentService.enrollForTheAppointment(id);
    }

    @PreAuthorize("hasRole('HOSPITAL')")
    @PatchMapping("/{id}/hospital/actions/enroll")
    public ResponseEntity<Appointment> enrollByHospital(@PathVariable final Long id,
                                                        @RequestBody final HospitalEnrollDto hospitalEnrollDto) {
        return appointmentService.enrollForTheAppointment(id, hospitalEnrollDto);
    }

    @PreAuthorize("hasRole('HOSPITAL')")
    @PatchMapping("/{id}/actions/made")
    public ResponseEntity<Appointment> markAsMade(@PathVariable Long id) {
        return appointmentService.markAppointmentAsMade(id);
    }

    @PreAuthorize("hasRole('HOSPITAL')")
    @PatchMapping("/{id}/actions/not-made")
    public ResponseEntity<Appointment> markAppointmentAsNotMade(@PathVariable Long id) {
        return appointmentService.markAppointmentAsNotMade(id);
    }

}


