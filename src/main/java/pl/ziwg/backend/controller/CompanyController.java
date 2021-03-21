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
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.service.CompanyService;

import javax.validation.Valid;

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
        return new ResponseEntity<>(companyService.findAll(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getOne(@PathVariable Long id) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));
        return new ResponseEntity<>(company, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Company> delete(@PathVariable Long id) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));
        companyService.delete(company);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("")
    public ResponseEntity<Company> newCompany(@Valid @RequestBody Company newCompany) {
        return new ResponseEntity<>(companyService.save(newCompany), HttpStatus.CREATED);
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
