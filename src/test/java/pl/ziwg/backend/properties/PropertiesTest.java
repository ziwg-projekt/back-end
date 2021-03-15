package pl.ziwg.backend.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class PropertiesTest {
    @Autowired
    DatabaseProperties databaseProperties = new DatabaseProperties();

    @Test
    public void contextLoads() {
    }

    @Test
    public void testTakingDatabaseProperties(){
        assertTrue(databaseProperties.getPort()==3006);
        assertTrue(databaseProperties.getName().equals("ziwg-db"));
    }
}
