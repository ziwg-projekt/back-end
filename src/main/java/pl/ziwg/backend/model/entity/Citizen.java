package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.ziwg.backend.model.enumerates.CitizenState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "citizen")
@Getter
@ToString
@Setter
@NoArgsConstructor
public class Citizen {
    @Id
    private String pesel;

    @NotNull
    private String name;

    @NotNull
    private String surname;

    @NotNull
    @JsonProperty(value="phone_number")
    private String phoneNumber;

    private String email;

    @ManyToOne()
    @JoinColumn(name = "hospital", referencedColumnName = "id")
    private Hospital hospital;

    private CitizenState state;

    @JsonIgnore
    @OneToMany(mappedBy="citizen")
    private Set<Appointment> appointments;





}
