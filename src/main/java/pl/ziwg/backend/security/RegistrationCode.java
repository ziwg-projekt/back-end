package pl.ziwg.backend.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@ToString
public class RegistrationCode {
    private String code;
    private LocalDateTime expireTime;
    private LocalDateTime startTime;
    private int expireIn;

    public RegistrationCode(String code, int expireIn) {
        this.code = code;
        this.code = "123456";   // TODO: to be deleted when communications systems will be implemented
        this.expireIn = expireIn;
        this.startTime = LocalDateTime.now();
        this.expireTime = startTime.plusSeconds(expireIn);
    }

    public boolean isExpire() {
        return LocalDateTime.now().isAfter(expireTime);
    }

    public long getHowManyCodeExistsInSeconds(){
        return ChronoUnit.SECONDS.between(startTime, LocalDateTime.now());
    }




}
