package pl.ziwg.backend.properties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "database")
public class DatabaseProperties {
    private int port;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public int getPort(){
        return port;
    }

    public void setPort(int port){
        this.port = port;
    }
}
