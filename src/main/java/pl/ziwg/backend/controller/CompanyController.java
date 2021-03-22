package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import pl.ziwg.backend.exception.ApiError;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.ImageHandler;
import pl.ziwg.backend.model.entity.Company;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.service.CompanyService;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

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

    @PostMapping("/{id}/logo")
    public ResponseEntity<Company> uploadImage(@PathVariable Long id, @RequestParam("imageFile") MultipartFile file) throws IOException {
        System.out.println("Original Image Byte Size - " + file.getBytes().length);
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));
        company.setLogoByte(ImageHandler.compressBytes(file.getBytes()));
        companyService.save(company);
        return new ResponseEntity<>(company, HttpStatus.CREATED);
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id){
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));
        return new ResponseEntity<>(ImageHandler.decompressBytes(company.getLogoByte()), HttpStatus.OK);
    }

    @GetMapping("/{id}/vaccines")
    public ResponseEntity<Set<Vaccine>> getVaccines(@PathVariable Long id) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));
        return new ResponseEntity<>(company.getVaccines(), HttpStatus.OK);
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

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNoSuchResourceException(ResourceNotFoundException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException exception) {
        return new ResponseEntity<>(new ApiError(exception), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiError> handleMultipartException(MultipartException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }
}
