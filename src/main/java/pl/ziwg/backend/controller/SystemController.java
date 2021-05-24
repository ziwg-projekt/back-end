package pl.ziwg.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.ziwg.backend.threads.AppointmentsThread;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {
    private AppointmentsThread appointmentsThread;

    @Autowired
    public SystemController(AppointmentsThread appointmentsThread){
        this.appointmentsThread = appointmentsThread;
    }

    @GetMapping("")
    public ResponseEntity<String> getAll() {
        return new ResponseEntity<>("to implement", HttpStatus.NOT_IMPLEMENTED);
    }
}
