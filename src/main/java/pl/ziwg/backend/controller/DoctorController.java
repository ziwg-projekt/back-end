package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.service.DoctorService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {
    private DoctorService doctorService;

    @Autowired
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
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

    @PostMapping("")
    public ResponseEntity<Doctor> newDoctor(@Valid @RequestBody Doctor newDoctor) {
        return new ResponseEntity<>(doctorService.save(newDoctor), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<Map<String, Object>>> getCitizenAppointments(@PathVariable Long id) throws IllegalAccessException {
        Doctor doctor = doctorService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "doctor"));
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(doctor.getAppointments(), Arrays.asList("doctor"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
