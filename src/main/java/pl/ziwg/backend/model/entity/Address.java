package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.ziwg.backend.externalapi.opencagedata.GeocodeRepository;
import pl.ziwg.backend.externalapi.opencagedata.GeocodeRepositoryImpl;
import pl.ziwg.backend.externalapi.opencagedata.entity.GeocodeResponse;

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
import java.util.Objects;


@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
public class Address  {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "address")
    private Hospital hospital;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "address")
    private Citizen citizen;


    @NotEmpty
    private String city;

    @NotEmpty
    private String street;

    @JsonProperty(value="street_number")
    @NotEmpty
    private String streetNumber;

    @NotNull
    @Valid
    private float latitude;

    @NotNull
    @Valid
    private float longitude;

    public Address(String city, String street, String streetNumber){
        this.city = city;
        this.street = street;
        this.streetNumber = streetNumber;
        GeocodeRepository geocodeRepository = new GeocodeRepositoryImpl(System.getenv("OPENCAGEDATA_API_KEY"));
        GeocodeResponse response = geocodeRepository.query(city + " " + street + " " + streetNumber);
        if(Objects.nonNull(response.getResults())) {
            if(response.getResults().isEmpty()){
                this.longitude = 0;
                this.latitude = 0;
            }
            else {
                this.longitude = response.getResults().get(0).getGeometry().getLongitude();
                this.latitude = response.getResults().get(0).getGeometry().getLatitude();
            }
        }
    }


    @Override
    public String toString() {
        return "Address{" +
                "id=" + id +
                ", city='" + city + '\'' +
                ", street='" + street + '\'' +
                ", streetNumber='" + streetNumber + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
