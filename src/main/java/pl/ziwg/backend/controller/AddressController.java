package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ziwg.backend.model.entity.Address;
import pl.ziwg.backend.service.AddressService;


@RestController
@RequestMapping("/api/v1/address")
public class AddressController {
    private AddressService addressService;

    @Autowired
    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/get-all")
    public Iterable<Address> getAll() {
        return addressService.findAll();
    }
}
