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
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.enumerates.CitizenState;
import pl.ziwg.backend.service.CitizenService;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/v1/citizens")
public class CitizenController {
    private CitizenService citizenService;

    @Autowired
    public CitizenController(CitizenService citizenService) {
        this.citizenService = citizenService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Citizen>> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
        return new ResponseEntity<>(citizenService.findAllFromPage(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{pesel}")
    public ResponseEntity<Citizen> getOne(@PathVariable String pesel) {
        Citizen citizen = citizenService.findByPesel(pesel)
                .orElseThrow(() -> new ResourceNotFoundException(pesel, "citizen"));
        return new ResponseEntity<>(citizen, HttpStatus.OK);
    }

    @GetMapping("/{pesel}/appointments")
    public ResponseEntity<List<Map<String, Object>>> getCitizenAppointments(@PathVariable String pesel) throws IllegalAccessException {
        Citizen citizen = citizenService.findByPesel(pesel)
                .orElseThrow(() -> new ResourceNotFoundException(pesel, "citizen"));
        List<Map<String, Object>> response = EntityConverter.getListRepresentationWithoutChosenFields(citizen.getAppointments(), Arrays.asList("citizen"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Citizen> newCitizen(@Valid @RequestBody Citizen newCitizen) {
        //TODO: validate if phone corresponds to PESEL in external database
        newCitizen.setState(CitizenState.WAITING);
        return new ResponseEntity<>(citizenService.save(newCitizen), HttpStatus.CREATED);
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
