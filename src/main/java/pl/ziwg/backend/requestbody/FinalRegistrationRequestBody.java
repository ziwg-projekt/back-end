package pl.ziwg.backend.requestbody;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FinalRegistrationRequestBody {
    @NotNull
    private String password;

    @NotNull
    private String username;
}