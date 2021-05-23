package pl.ziwg.backend.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ziwg.backend.dto.CitizenUpdateDto;
import pl.ziwg.backend.dto.CitizenUpdateResponseDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.service.CitizenService;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

    @GetMapping("/{pesel}")
    public ResponseEntity<Citizen> getOne(@PathVariable String pesel) {
        Citizen citizen = citizenService.findByPesel(pesel)
                .orElseThrow(() -> new ResourceNotFoundException(pesel, "citizen"));
        return new ResponseEntity<>(citizen, HttpStatus.OK);
    }

    @GetMapping("/{pesel}/appointments")
    public ResponseEntity<Set<Appointment>> getCitizenAppointments(@PathVariable String pesel) {
        Citizen citizen = citizenService.findByPesel(pesel)
                .orElseThrow(() -> new ResourceNotFoundException(pesel, "citizen"));
        return new ResponseEntity<>(citizen.getAppointments(), HttpStatus.OK);
    }

    @ApiOperation(value = "Update citizen data")
    @PutMapping("/{pesel}")
    public ResponseEntity<CitizenUpdateResponseDto> updateCitizenData(
            @RequestBody @Valid final CitizenUpdateDto citizenDataDto,
            @PathVariable final String pesel) {
        return new ResponseEntity<>(citizenService.updateCitizenData(citizenDataDto, pesel), HttpStatus.OK);
    }
}
