package pl.ziwg.backend.notificator.sms;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.Objects;

@Service
public class SMSSender {
    protected static final Logger log = LoggerFactory.getLogger(SMSSender.class);

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

        try {
            Message.creator(new PhoneNumber(phoneNumber), new PhoneNumber(myPhoneNumber), message).create();
            log.info("sms sent successfully");
        } catch (Exception e){
            log.info("SMS not sent cause of " + e.getMessage());
        }
    }
}
