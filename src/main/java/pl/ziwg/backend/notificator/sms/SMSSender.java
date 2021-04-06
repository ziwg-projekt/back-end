package pl.ziwg.backend.notificator.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Service
public class SMSSender {
    @Value("${sms.account.sid}")
    private String accountSid;

    @Value("${sms.auth.id}")
    private String authId;

    @Value("${sms.number}")
    private String myPhoneNumber;

    @Async
    public void sendMessage(@NotNull final String phoneNumber, @NotNull final String message) {
        if (Objects.nonNull(accountSid) && Objects.nonNull(authId)) {
            Twilio.init(accountSid, authId);
        } else {
            throw new RuntimeException("account SID or Auth id were not loaded");
        }

        Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(myPhoneNumber), message).create();
    }
}
