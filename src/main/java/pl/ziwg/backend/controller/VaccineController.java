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
import pl.ziwg.backend.model.EntityConverter;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.model.enumerates.VaccineState;
import pl.ziwg.backend.service.VaccineService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vaccines")
public class VaccineController {
    private VaccineService vaccineService;

    @Autowired
    public VaccineController(VaccineService vaccineService){
        this.vaccineService = vaccineService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Vaccine>> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
        return new ResponseEntity<>(vaccineService.findAllFromPage(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Vaccine> getOne(@PathVariable String code) {
        Vaccine vaccine = vaccineService.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(code, "vaccine"));
        return new ResponseEntity<>(vaccine, HttpStatus.OK);
    }

    @GetMapping("/{code}/appointment")
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable String code) throws IllegalAccessException {
        Vaccine vaccine = vaccineService.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(code, "vaccine"));
        Map<String, Object> appointment = EntityConverter.getRepresentationWithoutChosenFields(vaccine.getAppointment(), Arrays.asList("vaccine"));
        return new ResponseEntity<>(appointment, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Vaccine> newVaccine(@Valid @RequestBody Vaccine newVaccine) {
        newVaccine.setState(VaccineState.AVAILABLE);
        return new ResponseEntity<>(vaccineService.save(newVaccine), HttpStatus.CREATED);
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
