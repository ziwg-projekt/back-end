package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.service.entityaccess.HospitalAccessService;

@RestController
@RequestMapping("/api/v1/hospital")
public class HospitalController {
    private HospitalAccessService hospitalAccessService;

    @Autowired
    public HospitalController(HospitalAccessService hospitalAccessService) {
        this.hospitalAccessService = hospitalAccessService;
    }

    @GetMapping("/get-all")
    public Iterable<Hospital> getAll() {
        return hospitalAccessService.findAll();
    }
}
