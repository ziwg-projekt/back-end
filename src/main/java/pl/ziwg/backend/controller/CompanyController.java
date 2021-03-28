package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.service.CompanyService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    private CompanyService companyService;

    @Autowired
    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Company>> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
        return new ResponseEntity<>(companyService.findAllFromPage(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getOne(@PathVariable Long id) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));
        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @PostMapping("/{id}/logo")
    public ResponseEntity<Company> uploadImage(@PathVariable Long id, @RequestParam("imageFile") MultipartFile file) throws IOException {
        System.out.println("Original Image Byte Size - " + file.getBytes().length);
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));
        company.setLogoByte(file.getBytes());
        companyService.save(company);
        return new ResponseEntity<>(company, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id){
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));
        byte[] logo = {};
        if(company.getLogoByte()!=null){
            logo = company.getLogoByte();
        }
        return new ResponseEntity<>(logo, HttpStatus.OK);
    }

    @GetMapping("/{id}/vaccines")
    public ResponseEntity<List<Map<String, Object>>> getVaccines(@PathVariable Long id){
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));
        List<Map<String, Object>> response = EntityToMapConverter.getListRepresentationWithoutChosenFields(company.getVaccines(), Arrays.asList("company", "appointment"));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Company> delete(@PathVariable Long id) {
        Optional<Company> company = companyService.findById(id);
        company.ifPresent(value -> companyService.delete(value));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("")
    public ResponseEntity<Company> newCompany(@Valid @RequestBody Company newCompany) {
        return new ResponseEntity<>(companyService.save(newCompany), HttpStatus.CREATED);
    }


}
