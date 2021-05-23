package pl.ziwg.backend.threads;

import com.github.javafaker.App;
import lombok.SneakyThrows;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.service.AppointmentService;
import pl.ziwg.backend.service.DoctorService;

import java.time.LocalDateTime;

public class AppointmentsThread implements Runnable, DisposableBean {
    private AppointmentService appointmentService;
    private volatile boolean active = true;
    protected static final Logger log = Logger.getLogger(BackendApplication.class);

    @Autowired
    AppointmentsThread(AppointmentService appointmentService){
        this.appointmentService = appointmentService;
    }

    @SneakyThrows
    @Override
    public void run() {
        while(active){
            Thread.sleep(2000);
            for(Appointment appointment : appointmentService.findAll()){
                System.out.println("gram");
                if(appointment.getDate().isBefore(LocalDateTime.now()) && (appointment.getState().equals(AppointmentState.AVAILABLE))){
                    log.info("Past appointment with AVAILABLE state was found - " + appointment);
                    Hospital hospital = appointment.getHospital();
                    appointmentService.createAppointmentInsteadOfTheOldOne(appointment, hospital);
                }
            }
        }
    }

    @Override
    public void destroy(){
        active = false;
    }
}
