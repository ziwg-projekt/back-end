package pl.ziwg.backend.externalapi.opencagedata;

import org.junit.jupiter.api.Test;
import pl.ziwg.backend.externalapi.opencagedata.entity.GeocodeResponse;

import java.text.ChoiceFormat;

import static org.junit.jupiter.api.Assertions.*;

public class OpencageTest {
    GeocodeRepository geocodeRepository = new GeocodeRepositoryImpl(System.getenv("OPENCAGEDATA_API_KEY"));

    @Test
    public void testIfApiKeyIsInEnvironmentVariables(){
        System.out.println(System.getenv("OPENCAGEDATA_API_KEY"));
        assertNotEquals(null, System.getenv("OPENCAGEDATA_API_KEY"));
    }
    @Test
    public void getInfoByCoords() {
        GeocodeResponse response = geocodeRepository.query("50.23+49.42");
        assertEquals("West Kazakhstan Region", response.getResults().get(0).getComponents().getState());
        assertEquals("200", response.getStatus().getCode());
    }

    @Test
    public void getInfoByTitle() {
        GeocodeResponse response = geocodeRepository.query("Zielinskiego 49 Wroclaw");
        assertEquals("50-089", response.getResults().get(0).getComponents().getPostcode());
        assertEquals("200", response.getStatus().getCode());
    }
}
