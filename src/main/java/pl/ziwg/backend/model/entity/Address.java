package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "address")
@Getter
@ToString
@Setter
@NoArgsConstructor
public class Address {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long Id;

    @NotEmpty
    private String city;

    @NotEmpty
    private String street;

    @JsonProperty(value="house_number")
    @NotEmpty
    private String houseNumber;


    @NotNull
    @Valid
    private float latitude;

    @NotNull
    @Valid
    private float longitude;

}
