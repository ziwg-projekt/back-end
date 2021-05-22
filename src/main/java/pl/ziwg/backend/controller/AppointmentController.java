package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import pl.ziwg.backend.dto.VaccineDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.exception.UserTypeException;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.model.enumerates.UserType;
import pl.ziwg.backend.model.enumerates.VaccineState;
import pl.ziwg.backend.notificator.email.EmailSubject;
import pl.ziwg.backend.security.jwt.service.UserPrinciple;
import pl.ziwg.backend.service.AppointmentService;
import pl.ziwg.backend.service.EmailService;
import pl.ziwg.backend.service.UserService;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {
    private AppointmentService appointmentService;
    private UserService userService;
    private EmailService emailService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService, UserService userService,
                                 EmailService emailService) {
        this.appointmentService = appointmentService;
        this.userService = userService;
        this.emailService = emailService;
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
        appointment.getVaccine().setState(VaccineState.AVAILABLE);
        appointmentService.save(appointment);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PatchMapping("/{id}/actions/enroll")
    public ResponseEntity<Appointment> enroll(@PathVariable Long id) {
        User user = userService.getUserFromContext();
        Appointment appointment = appointmentService.findById(id)
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
    @PatchMapping("/{id}/hospital/actions/enroll")
    public ResponseEntity<Appointment> enrollByHospital(@PathVariable final Long id, @RequestParam final Long userId) {
        final User user = userService.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userId, "user"));
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
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "appointment"));
        if(appointment.getHospital().equals(userService.getUserFromContext().getHospital())) {
            appointment.setState(AppointmentState.MADE);
            appointment.getVaccine().setState(VaccineState.GIVEN);
            appointmentService.save(appointment);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PreAuthorize("hasRole('HOSPITAL')")
    @PatchMapping("/{id}/actions/not-made")
    public ResponseEntity<Appointment> markAsNotCame(@PathVariable Long id) {
        Appointment appointment = appointmentService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "appointment"));
        if(appointment.getHospital().equals(userService.getUserFromContext().getHospital())) {
            VaccineDto vaccine = new VaccineDto(appointment.getVaccine().getCode(), appointment.getVaccine().getCompany().getName());
            appointmentService.delete(id);
            appointmentService.createAppointment(userService.getUserFromContext().getHospital(), vaccine);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        else{
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    private String parseDate(LocalDateTime time) {
        return String.format("%d.%d.%d %d:%d", time.getDayOfMonth(), time.getMonthValue(), time.getYear(),
                time.getHour(), time.getMinute());
    }

}


