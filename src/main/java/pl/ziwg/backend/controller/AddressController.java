package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.ziwg.backend.exception.InvalidRequestException;
import pl.ziwg.backend.externalapi.opencagedata.GeocodeRepository;
import pl.ziwg.backend.externalapi.opencagedata.GeocodeRepositoryImpl;
import pl.ziwg.backend.externalapi.opencagedata.entity.GeocodeResponse;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.model.modelassembler.AddressModelAssembler;
import pl.ziwg.backend.service.AddressService;
import pl.ziwg.backend.exception.AddressNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;


@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {
    private AddressService addressService;
    private AddressModelAssembler assembler;


    @Autowired
    public AddressController(AddressService addressService, AddressModelAssembler assembler) {
        this.addressService = addressService;
        this.assembler = assembler;
    }

    @GetMapping("")
    public CollectionModel<EntityModel<Address>> getAll() {

        List<EntityModel<Address>> employees = addressService.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());
        
        return CollectionModel.of(employees, linkTo(methodOn(AddressController.class).getAll()).withSelfRel());
    }

    @PostMapping("")
    public ResponseEntity<?> newAddress(@RequestBody Address newAddress) {

        EntityModel<Address> entityModel = assembler.toModel(addressService.save(newAddress));

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
                .body(entityModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> replaceAddress(@RequestBody Address newAddress, @PathVariable Long id) {

        Address updatedEmployee = addressService.findById(id)
                .map(address -> {
                    address.setLatitude(newAddress.getLatitude());
                    address.setLongitude(newAddress.getLongitude());
                    return addressService.save(address);
                })
                .orElseGet(() -> {
                    newAddress.setId(id);
                    return addressService.save(newAddress);
                });

        EntityModel<Address> entityModel = assembler.toModel(updatedEmployee);

        return ResponseEntity
                .created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri())
                .body(entityModel);
    }

    @GetMapping("/{id}")
    public EntityModel<Address> getOne(@PathVariable Long id) {
        Address address = addressService.findById(id)
                .orElseThrow(() -> new AddressNotFoundException(id));

        return assembler.toModel(address);
    }

    @GetMapping("/generate")
    @ResponseBody
    public GeocodeResponse generateAddress(@RequestParam(required = false) Optional<String> lat,
                                           @RequestParam(required = false) Optional<String> lon,
                                           @RequestParam(required = false) Optional<String> title){
        String query = "";
//        String apiKey = System.getenv("OPENCAGEDATA_API_KEY");
        String apiKey = "84aa8a50b45c4ec7b1c9f39164b39521";
        if(title.isPresent()){
            query = title.get();
        }
        else if(lon.isPresent() && lat.isPresent()){
            query = lat.get() + "+" + lon.get();
        }
        else{
            throw new InvalidRequestException("You must title or latitude and longitude in request parameters!");
        }

        GeocodeRepository geocodeRepository = new GeocodeRepositoryImpl(apiKey);
        return geocodeRepository.query(query);
    }

}
