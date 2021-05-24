package pl.ziwg.backend.threads;

import com.github.javafaker.App;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.model.entity.Hospital;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.notificator.email.EmailSubject;
import pl.ziwg.backend.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class AppointmentsThread implements Runnable, DisposableBean {
    private AppointmentService appointmentService;
    private DoctorService doctorService;
    private SMSService smsService;
    private EmailService emailService;
    private volatile boolean active = true;
    protected static final Logger log = LoggerFactory.getLogger(AppointmentsThread.class);
    private Thread thread;

    @Autowired
    AppointmentsThread(AppointmentService appointmentService, DoctorService doctorService, EmailService emailService, SMSService smsService){
        this.thread = new Thread(this);
        this.thread.start();
        this.appointmentService = appointmentService;
        this.doctorService = doctorService;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    @SneakyThrows
    @Override
    public void run() {
        while(active){
            Thread.sleep(2000);
            checkIfDoctorsHasNoNextAppointmentDatesInPast(doctorService.findAll());
            checkIfThereIsNoAvailableAppointmentsInPast(appointmentService.findAll());
            checkIfAllRemindersAreSend(appointmentService.findAll());
        }
    }

    private void checkIfDoctorsHasNoNextAppointmentDatesInPast(List<Doctor> doctors){
        for(Doctor doctor : doctors){
            LocalDateTime appointmentDate = doctor.getNextAppointmentDate();
            if(appointmentDate.isBefore(LocalDateTime.now())){
                doctor.setNextAppointmentDate(AppointmentDateService.getNextValidAppointmentDateFromGivenDate(LocalDateTime.now()));
                doctorService.save(doctor);
                log.info("Past next appointment date at doctor with id = " + doctor.getId() + " was found - " + appointmentDate + ", changed to " + doctor.getNextAppointmentDate());
            }
        }
    }

    private void checkIfThereIsNoAvailableAppointmentsInPast(List<Appointment> appointments){
        for(Appointment appointment : appointments){
            if(appointment.getDate().isBefore(LocalDateTime.now()) && (appointment.getState().equals(AppointmentState.AVAILABLE))){
                log.info("Past appointment with AVAILABLE state was found - " + appointment);
                Hospital hospital = appointment.getHospital();
                appointmentService.createAppointmentInsteadOfTheOldOne(appointment, hospital);
            }
        }
    }

    private void checkIfAllRemindersAreSend(List<Appointment> appointments){
        for(Appointment appointment : appointments){
            if(!appointment.isIfReminded() && appointment.getState().equals(AppointmentState.ASSIGNED)){
                if(getHoursDifferenceBetweenDates(appointment.getDate(), LocalDateTime.now()) < 24){
                    smsService.sendAppointmentReminder(appointment.getCitizen().getPhoneNumber(), appointment.getDate());
                    emailService.sendAppointmentReminder(appointment.getCitizen().getEmail(), appointment.getDate(), EmailSubject.APPOINTMENT_REMINDER);
                    log.info("Sent appointment reminder to " + appointment.getCitizen() + " about appointment " + appointment);
                    appointment.setIfReminded(true);
                    appointmentService.save(appointment);
                }
            }
        }
    }

    private long getHoursDifferenceBetweenDates(LocalDateTime fromDate, LocalDateTime toDate){
        return ChronoUnit.HOURS.between(fromDate, toDate);
    }

    @Override
    public void destroy(){
        active = false;
    }
}
