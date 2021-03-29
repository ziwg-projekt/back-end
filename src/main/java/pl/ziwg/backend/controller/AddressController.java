package pl.ziwg.backend.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.IncorrectRequestParametersException;
import pl.ziwg.backend.externalapi.opencagedata.GeocodeRepository;
import pl.ziwg.backend.externalapi.opencagedata.GeocodeRepositoryImpl;
import pl.ziwg.backend.externalapi.opencagedata.entity.GeocodeResponse;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.service.AddressService;
import pl.ziwg.backend.exception.ResourceNotFoundException;

import javax.validation.Valid;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {
    private AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("")
    public ResponseEntity<Page<Address>> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
            return new ResponseEntity<>(addressService.findAllFromPage(pageRequest), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getOne(@PathVariable Long id) {
        Address address = addressService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "address"));
        return new ResponseEntity<>(address, HttpStatus.OK);
    }

    @GetMapping("/generate")
    @ApiOperation(value = "Generate address depends on given request parameters", notes = "it is possible to generate " +
            "address from e.g. '?title=Wybrzeze Wyspanskiego 27' or '?lat=50.67&lon=18.31'")
    public ResponseEntity<GeocodeResponse> generateAddress(@RequestParam(required = false) Optional<Float> lat,
                                           @RequestParam(required = false) Optional<Float> lon,
                                           @RequestParam(required = false) Optional<String> title){
        String apiKey = System.getenv("OPENCAGEDATA_API_KEY");
        GeocodeRepository geocodeRepository = new GeocodeRepositoryImpl(apiKey);
        if(title.isPresent()){
            return new ResponseEntity<>(geocodeRepository.query(title.get()), HttpStatus.OK);
        }
        else if(lon.isPresent() && lat.isPresent()){
            return new ResponseEntity<>(geocodeRepository.reverse(lat.get(), lon.get()), HttpStatus.OK);
        }
        else{
            throw new IncorrectRequestParametersException("You must include title or latitude and longitude in request parameters!");
        }
    }
}
