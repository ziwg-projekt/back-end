package pl.ziwg.backend.externalapi.governmentapi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Person {

    @JsonIgnore
    private long id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String surname;

    @NotEmpty
    private String pesel;

    @JsonProperty("phone_number")
    private String phoneNumber;
    private String email;

    public Optional<String> getPhoneNumber() {
        return Optional.ofNullable(phoneNumber);
    }

    public Optional<String> getEmail() {
        return Optional.ofNullable(email);
    }


    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", pesel='" + pesel + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return pesel.equals(person.pesel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pesel);
    }
}
