package pl.ziwg.backend.security;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RegistrationCode {
    private String code;
    private LocalDateTime expireTime;

    public RegistrationCode(String code, int expireIn) {
        this.code = code;
        this.code = "123456";
        this.expireTime = LocalDateTime.now().plusSeconds(expireIn);
    }

    public boolean isExpire() {
        return LocalDateTime.now().isAfter(expireTime);
    }
}
