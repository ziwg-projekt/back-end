package pl.ziwg.backend.externalapi.governmentapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class GovernmentTest {

    private PersonRegister personRegister;
    private final static String validPesel = "99110323923";

    @Autowired
    public GovernmentTest(PersonRegister personRegister){
        this.personRegister = personRegister;
    }

    @Test
    public void checkIfCorrectNameForGivenPesel(){
       Person person = personRegister.getPersonByPesel(validPesel);
       assertThat(person.getName()).isEqualTo("Jan");
    }
}
