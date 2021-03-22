package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private String phoneNumber;

    private String email;

    @ManyToOne()
    @JoinColumn(name = "hospital", referencedColumnName = "id", nullable = false)
    private Hospital hospital;

    private CitizenState state;

    @JsonIgnore
    @OneToMany(mappedBy="citizen")
    private Set<Appointment> appointments;





}
