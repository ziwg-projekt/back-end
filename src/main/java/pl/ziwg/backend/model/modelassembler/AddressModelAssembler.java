package pl.ziwg.backend.model.modelassembler;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;
import pl.ziwg.backend.controller.AddressController;
import pl.ziwg.backend.model.entity.Address;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class AddressModelAssembler implements RepresentationModelAssembler<Address, EntityModel<Address>> {
    @Override
    public EntityModel<Address> toModel(Address entity) {
        return EntityModel.of(entity, //
                linkTo(methodOn(AddressController.class).getOne(entity.getId())).withSelfRel(),
                linkTo(methodOn(AddressController.class).getAll()).withRel("addresses"));
    }


}
