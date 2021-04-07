package pl.ziwg.backend.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "government")
public class GovernmentApiProperties {
    private String username;
    private String password;
}
