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
        final Citizen citizen = citizenService.findByPesel(hospitalEnrollDto.getPesel())
                .orElseThrow(() -> new ResourceNotFoundException(hospitalEnrollDto.getPesel(), "pesel"));
        final User user = userService.findById(citizen.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException(citizen.getUser().getId(), "user"));
        if (UserType.CITIZEN != user.getUserType()) {
            throw new UserTypeException("Given user id must belong to the citizen");
        }
        final Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "appointment"));

        appointment.setState(AppointmentState.ASSIGNED);
        appointment.getVaccine().setState(VaccineState.ASSIGNED_TO_CITIZEN);
        appointment.setCitizen(user.getCitizen());
        appointmentService.save(appointment);
        if (Objects.nonNull(user.getCitizen().getEmail())) {
            emailService.sendVisitConfirmation(user.getCitizen().getEmail(), parseDate(appointment.getDate()),
                    EmailSubject.REGISTRATION_FOR_VACCINATION, user.getCitizen().getName());
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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

    private String parseDate(LocalDateTime time) {
        return String.format("%d.%d.%d %d:%d", time.getDayOfMonth(), time.getMonthValue(), time.getYear(),
                time.getHour(), time.getMinute());
    }
}


