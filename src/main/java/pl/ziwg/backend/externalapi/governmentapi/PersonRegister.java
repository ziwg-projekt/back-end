package pl.ziwg.backend.externalapi.governmentapi;

import org.springframework.stereotype.Component;

@Component
public class PersonRegister {
    //TODO: define fields

    public boolean checkIfPeselExists(String pesel){
        //TODO: request to government api
        return true;
    }

    public Person getPersonByPesel(String pesel){
        //TODO: request to government api
        return new Person("Radek", "Kowalski", pesel, "666999888", "lisradoslaw0@gmail.com");
    }
}
