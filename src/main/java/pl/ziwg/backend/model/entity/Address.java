package pl.ziwg.backend.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

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

    private String city;
    private String street;
    private String houseNumber;
    private float latitude;
    private float longitude;

}
