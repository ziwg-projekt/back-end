package pl.ziwg.backend.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.jsonbody.response.UploadFileResponse;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.service.CompanyService;
import pl.ziwg.backend.service.FileStorageService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {
    protected static final Logger log = Logger.getLogger(BackendApplication.class);

    private CompanyService companyService;
    private FileStorageService fileStorageService;

    @Autowired
    public CompanyController(CompanyService companyService, FileStorageService fileStorageService) {
        this.companyService = companyService;
        this.fileStorageService = fileStorageService;
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
    public ResponseEntity<UploadFileResponse> uploadLogo(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        Company company = companyService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "company"));

        String fileName = fileStorageService.storeFile(file);

        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/v1/companies/logo/")
                .path(fileName)
                .toUriString();

        company.setLogoPath(fileDownloadUri);
        companyService.save(company);
        return new ResponseEntity<>(new UploadFileResponse(fileName, fileDownloadUri,
                file.getContentType(), file.getSize()), HttpStatus.CREATED);
    }

    @GetMapping("/logo/{fileName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String fileName, HttpServletRequest request){
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("Could not determine file type.");
        }

        if(contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
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
