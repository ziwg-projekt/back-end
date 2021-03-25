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
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.service.HospitalService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        return new ResponseEntity<>(hospitalService.findAllFromPage(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hospital> getOne(@PathVariable Long id) {
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        return new ResponseEntity<>(hospital, HttpStatus.OK);
    }

    @GetMapping("/{id}/vaccines")
    public ResponseEntity<List<Map<String, Object>>> getVaccines(@PathVariable Long id) throws IllegalAccessException {
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(hospital.getVaccines(), Arrays.asList("hospital", "appointment"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/citizens")
    public ResponseEntity<List<Map<String, Object>>> getCitizens(@PathVariable Long id) throws IllegalAccessException {
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(hospital.getCitizens(), Arrays.asList("hospital", "appointments"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<List<Map<String, Object>>> getDoctors(@PathVariable Long id) throws IllegalAccessException {
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(hospital.getDoctors(), Arrays.asList("hospital", "appointments"));
        return new ResponseEntity<>(response, HttpStatus.OK);
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
