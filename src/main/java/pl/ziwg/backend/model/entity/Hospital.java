package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "hospital")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String name;

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "address", referencedColumnName="Id")
    @NotNull
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy="hospital")
    private Set<Vaccine> vaccines;

    @JsonIgnore
    @OneToMany(mappedBy="hospital")
    private Set<Citizen> citizens;

    @JsonIgnore
    @OneToMany(mappedBy="hospital")
    private Set<Doctor> doctors;

    @JsonIgnore
    @OneToOne()
    private User user;

    public Hospital(String name, Address address){
        this.name = name;
        this.address = address;
    }

}
