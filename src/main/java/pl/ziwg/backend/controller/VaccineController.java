package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.service.VaccineService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/vaccines")
public class VaccineController {
    private VaccineService vaccineService;

    @Autowired
    public VaccineController(VaccineService vaccineService) {
        this.vaccineService = vaccineService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Vaccine>> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
        return new ResponseEntity<>(vaccineService.findAll(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vaccine> getOne(@PathVariable Long id) {
        Vaccine vaccine = vaccineService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "vaccine"));
        return new ResponseEntity<>(vaccine, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Vaccine> newAddress(@Valid @RequestBody Vaccine newVaccine) {
        return new ResponseEntity<>(vaccineService.save(newVaccine), HttpStatus.CREATED);
    }
}
