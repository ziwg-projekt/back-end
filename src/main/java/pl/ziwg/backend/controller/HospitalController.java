package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.entity.User;
import pl.ziwg.backend.service.AppointmentService;
import pl.ziwg.backend.service.HospitalService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/hospitals")
public class HospitalController {
    private HospitalService hospitalService;
    private AppointmentService appointmentService;

    @Autowired
    public HospitalController(HospitalService hospitalService, AppointmentService appointmentService) {
        this.hospitalService = hospitalService;
        this.appointmentService = appointmentService;
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
    public ResponseEntity<List<Map<String, Object>>> getVaccines(@PathVariable Long id){
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(hospital.getVaccines(), Arrays.asList("hospital", "appointment"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/vaccines/stats")
    public ResponseEntity<List<Map<String, Object>>> getAvailableVaccines(@PathVariable Long id){
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        List<Map<String, Object>> response = hospitalService.getVaccinesStatistics(hospital);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/{id}/citizens")
    public ResponseEntity<List<Map<String, Object>>> getCitizens(@PathVariable Long id){
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(hospital.getCitizens(), Arrays.asList("hospital", "appointments"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}/doctors")
    public ResponseEntity<List<Map<String, Object>>> getDoctors(@PathVariable Long id){
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(hospital.getDoctors(), Arrays.asList("hospital", "appointments"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<Hospital> newHospital(@Valid @RequestBody Hospital newHospital) {
        return new ResponseEntity<>(hospitalService.save(newHospital), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<Page<Appointment>> getAppointments(@PathVariable Long id, @PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest){
        Hospital hospital = hospitalService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "hospital"));
        Page<Appointment> appointments = appointmentService.findAllAvailableFromHospitalByPage(hospital, pageRequest);
        return new ResponseEntity<>(appointments, HttpStatus.OK);
    }

}
