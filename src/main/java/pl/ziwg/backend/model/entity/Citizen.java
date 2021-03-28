package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.ziwg.backend.externalapi.governmentapi.Person;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.enumerates.CitizenState;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Set;

@Entity
@Table(name = "citizen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Citizen {
    @Id
    private String pesel;

    @NotNull
    private String name;

    @NotNull
    private String surname;

    @NotNull
    @JsonProperty(value = "phone_number")
    private String phoneNumber;

    @Email
    @NotNull
    private String email;

    @ManyToOne()
    @JoinColumn(name = "hospital", referencedColumnName = "id")
    private Hospital hospital;

    private CitizenState state;

    @JsonIgnore
    @OneToMany(mappedBy="citizen", fetch = FetchType.EAGER)
    private Set<Appointment> appointments;

    @JsonIgnore
    @OneToOne()
    private User user;

    public Citizen(Person person){
        this.name = person.getName();
        this.surname = person.getSurname();
        this.pesel = person.getPesel();
        person.getPhoneNumber().ifPresent(s -> this.phoneNumber = s);
        person.getEmail().ifPresent(s -> this.email = s);
        this.state = CitizenState.WAITING;
    }

    @Override
    public String toString() {
        return "Citizen{" +
                "pesel='" + pesel + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                ", hospital=" + hospital +
                ", state=" + state +
                ", appointments=" + appointments +
                ", user=" + EntityToMapConverter.getRepresentationWithoutChosenFields(user, Arrays.asList("citizen")) +
                '}';
    }
}
