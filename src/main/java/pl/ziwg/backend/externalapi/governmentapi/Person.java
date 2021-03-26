package pl.ziwg.backend.externalapi.governmentapi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.ElementCollection;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
public class Person {
    @NotNull
    private String name;

    @NotNull
    private String surname;

    @NotNull
    private String pesel;
    private String phoneNumber;
    private String email;

    public Optional<String> getPhoneNumber(){
        return Optional.of(phoneNumber);
    }

    public Optional<String> getEmail(){
        return Optional.of(email);
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
        return name.equals(person.name) &&
                surname.equals(person.surname) &&
                pesel.equals(person.pesel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, pesel);
    }
}
