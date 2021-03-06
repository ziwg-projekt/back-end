package pl.ziwg.backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.dto.VaccineDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.model.enumerates.VaccineState;
import pl.ziwg.backend.service.AppointmentService;
import pl.ziwg.backend.service.UserService;
import pl.ziwg.backend.service.VaccineService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/vaccines")
public class VaccineController {
    private VaccineService vaccineService;
    private AppointmentService appointmentService;
    private UserService userService;

    @Autowired
    public VaccineController(VaccineService vaccineService, AppointmentService appointmentService, UserService userService){
        this.vaccineService = vaccineService;
        this.appointmentService = appointmentService;
        this.userService = userService;
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
    public ResponseEntity<Map<String, Object>> getAppointment(@PathVariable String code){
        Vaccine vaccine = vaccineService.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(code, "vaccine"));
        Map<String, Object> appointment = EntityToMapConverter.getRepresentationWithoutChosenFields(vaccine.getAppointment(), Arrays.asList("vaccine"));
        return new ResponseEntity<>(appointment, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('HOSPITAL')")
    @PostMapping("")
    public ResponseEntity<VaccineDto> newVaccine(@Valid @RequestBody VaccineDto newVaccine) {
        appointmentService.createAppointment(userService.getUserFromContext().getHospital(), newVaccine);
        return new ResponseEntity<>(newVaccine, HttpStatus.CREATED);
    }

}
