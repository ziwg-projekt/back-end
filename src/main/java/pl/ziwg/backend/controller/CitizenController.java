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
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.enumerates.CitizenState;
import pl.ziwg.backend.service.CitizenService;

import javax.validation.Valid;
import java.util.Set;

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

    @GetMapping("/{id}")
    public ResponseEntity<Citizen> getOne(@PathVariable Long id) {
        Citizen citizen = citizenService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "citizen"));
        return new ResponseEntity<>(citizen, HttpStatus.OK);
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<Set<Appointment>> getCitizenAppointments(@PathVariable Long id) {
        Citizen citizen = citizenService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "citizen"));
        return new ResponseEntity<>(citizen.getAppointments(), HttpStatus.OK);
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
