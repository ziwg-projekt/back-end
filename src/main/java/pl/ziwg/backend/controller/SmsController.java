package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ziwg.backend.notificator.sms.SMSSender;

@RestController
public class SmsController {
    private SMSSender smsSender;

    @Autowired
    public SmsController(SMSSender smsSender) {
        this.smsSender = smsSender;
    }

    //endpoint only for test on branch
    @GetMapping("/send/sms")
    public void sendSms() {
        //set your number for test
        String destinationPhoneNumber = "+48213721370";
        String msg = "Hello World!";
        smsSender.sendMessage(destinationPhoneNumber, msg);
    }
}
