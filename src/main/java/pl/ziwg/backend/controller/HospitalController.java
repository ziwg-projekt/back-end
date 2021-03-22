package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.ApiError;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.service.HospitalService;

import javax.validation.Valid;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/hospitals")
public class HospitalController {
    private HospitalService hospitalService;

    @Autowired
    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Hospital>> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
        return new ResponseEntity<>(hospitalService.findAll(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getOne(@PathVariable Long id) {
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        return new ResponseEntity<>(hospital, HttpStatus.OK);
    }

    @GetMapping("/{id}/vaccines")
    public ResponseEntity<Set<Vaccine>> getVaccines(@PathVariable Long id) {
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        return new ResponseEntity<>(hospital.getVaccines(), HttpStatus.OK);
    }

    @GetMapping("/{id}/citizens")
    public ResponseEntity<Set<Citizen>> getCitizens(@PathVariable Long id) {
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        return new ResponseEntity<>(hospital.getCitizens(), HttpStatus.OK);
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<Set<Doctor>> getDoctors(@PathVariable Long id) {
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        return new ResponseEntity<>(hospital.getDoctors(), HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Hospital> newHospital(@Valid @RequestBody Hospital newHospital) {
        return new ResponseEntity<>(hospitalService.save(newHospital), HttpStatus.CREATED);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNoSuchResourceException(ResourceNotFoundException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException exception) {
        return new ResponseEntity<>(new ApiError(exception), HttpStatus.BAD_REQUEST);
    }
}
