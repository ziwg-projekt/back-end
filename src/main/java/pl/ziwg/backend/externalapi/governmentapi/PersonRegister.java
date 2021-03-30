package pl.ziwg.backend.externalapi.governmentapi;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

@Component
public class PersonRegister {
    //TODO: define fields
    Faker faker = new Faker();

    public boolean checkIfPeselExists(String pesel){
        //TODO: request to government api
        return true;
    }

//    public Person getPersonByPesel(String pesel){
//        //TODO: request to government api
//        return new Person(faker.name().firstName(), faker.name().lastName(), pesel, "666999888", "lisradoslaw0@gmail.com");
//    }

    public Person getPersonByPeselMock(String pesel){
        //TODO: request to government api
        return new Person(faker.name().firstName(), faker.name().lastName(), pesel, "666999888", "lisradoslaw0@gmail.com");
    }
}
