package pl.ziwg.backend.jsonbody.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
@Setter
@ToString
public class JwtResponse {
    @JsonProperty("access_token")
    private String accessToken;
    private String type = "Bearer";
    private String username;

    @JsonProperty("user_id")
    private long userId;
    private Collection<? extends GrantedAuthority> authorities;

    public JwtResponse(String accessToken, String username, Collection<? extends GrantedAuthority> authorities, long userId) {
        this.accessToken = accessToken;
        this.username = username;
        this.authorities = authorities;
        this.userId = userId;
    }
}
