package pl.ziwg.backend.controller;

import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.ApiError;
import pl.ziwg.backend.exception.InvalidRequestException;
import pl.ziwg.backend.externalapi.opencagedata.GeocodeRepository;
import pl.ziwg.backend.externalapi.opencagedata.GeocodeRepositoryImpl;
import pl.ziwg.backend.externalapi.opencagedata.entity.GeocodeResponse;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.service.AddressService;
import pl.ziwg.backend.exception.AddressNotFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {
    private AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    public Page<Address> getAll(@PageableDefault(size = Integer.MAX_VALUE) Pageable pageRequest) {
            return addressService.findAll(pageRequest);
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public Address newAddress(@Valid @RequestBody Address newAddress) {
        return addressService.save(newAddress);
    }

    @PutMapping("/{id}")
    public Address replaceAddress(@RequestBody Address newAddress, @PathVariable Long id) {
        Address updatedAddress = addressService.findById(id)
                .map(address -> {
                    address.setLatitude(newAddress.getLatitude());
                    address.setLongitude(newAddress.getLongitude());
                    address.setCity(newAddress.getCity());
                    address.setHouseNumber(newAddress.getHouseNumber());
                    address.setStreet(newAddress.getStreet());
                    return addressService.save(address);
                })
                .orElseGet(() -> {
                    newAddress.setId(id);
                    return addressService.save(newAddress);
                });

        return updatedAddress;
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Address getOne(@PathVariable Long id) {
        Address address = addressService.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));
        return address;
    }

    @GetMapping("/generate")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation(value = "Generate address depends on given request parameters", notes = "it is possible to generate " +
            "address from e.g. '?title=Wybrzeze Wyspanskiego 27' or '?lat=50.67&lon=18.31'")
    public GeocodeResponse generateAddress(@RequestParam(required = false) Optional<Float> lat,
                                           @RequestParam(required = false) Optional<Float> lon,
                                           @RequestParam(required = false) Optional<String> title){
        String apiKey = System.getenv("OPENCAGEDATA_API_KEY");
        GeocodeRepository geocodeRepository = new GeocodeRepositoryImpl(apiKey);
        if(title.isPresent()){
            return geocodeRepository.query(title.get());
        }
        else if(lon.isPresent() && lat.isPresent()){
            return geocodeRepository.reverse(lat.get(), lon.get());
        }
        else{
            throw new InvalidRequestException("You must include title or latitude and longitude in request parameters!");
        }
    }

    @ExceptionHandler(AddressNotFoundException.class)
    @ResponseBody
    public ResponseEntity<Object> handleNoSuchAddressException(AddressNotFoundException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseBody
    public ResponseEntity<Object> handleNoSuchAddressException(InvalidRequestException exception) {
        return new ResponseEntity<>(new ApiError(exception.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(new ApiError(errors.toString()), HttpStatus.BAD_REQUEST);
    }

}
