package pl.ziwg.backend.jsonbody.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestBody {
    @NotNull
    private String password;

    @NotNull
    private String username;
}
