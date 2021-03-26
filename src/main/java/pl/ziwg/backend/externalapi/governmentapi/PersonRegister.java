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
        return new Person("Jan", "Kowalski", pesel, null, "janek@gmail.com");
    }
}
