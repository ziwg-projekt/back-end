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
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.security.jwt.service.UserPrinciple;
import pl.ziwg.backend.service.DoctorService;
import pl.ziwg.backend.service.UserService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {
    private DoctorService doctorService;
    private UserService userService;

    @Autowired
    public DoctorController(DoctorService doctorService, UserService userService) {
        this.doctorService = doctorService;
        this.userService = userService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Doctor>> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
        return new ResponseEntity<>(doctorService.findAllFromPage(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getOne(@PathVariable Long id) {
        Doctor doctor = doctorService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "doctor"));
        return new ResponseEntity<>(doctor, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HOSPITAL')")
    @PutMapping("")
    public ResponseEntity<Doctor> newDoctor() {
        return new ResponseEntity<>(doctorService.save(new Doctor(userService.getUserFromContext().getHospital())), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<Map<String, Object>>> getDoctorAppointments(@PathVariable Long id) {
        Doctor doctor = doctorService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "doctor"));
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(doctor.getAppointments(), Arrays.asList("doctor"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }



}
