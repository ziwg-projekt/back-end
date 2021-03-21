package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "address")
    private Hospital hospital;

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
