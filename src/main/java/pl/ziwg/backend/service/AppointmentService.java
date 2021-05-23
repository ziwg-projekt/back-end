package pl.ziwg.backend.service;

import com.github.javafaker.App;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.dto.AppointmentDto;
import pl.ziwg.backend.dto.VaccineDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.exception.VaccineAlreadyExistsException;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.model.enumerates.VaccineState;
import pl.ziwg.backend.model.repository.AppointmentRepository;
import pl.ziwg.backend.notificator.email.EmailSubject;

import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {
    private AppointmentRepository appointmentRepository;
    private VaccineService vaccineService;
    private CitizenService citizenService;
    private DoctorService doctorService;
    private CompanyService companyService;
    private UserService userService;
    private EmailService emailService;
    protected static final Logger log = Logger.getLogger(BackendApplication.class);

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, VaccineService vaccineService,
                              CitizenService citizenService, DoctorService doctorService, CompanyService companyService,
                              UserService userService, EmailService emailService) {
        this.appointmentRepository = appointmentRepository;
        this.vaccineService = vaccineService;
        this.citizenService = citizenService;
        this.doctorService = doctorService;
        this.companyService = companyService;
        this.userService = userService;
        this.emailService = emailService;
    }

    public Page<Appointment> findAllFromPage(Pageable pageable){
        return appointmentRepository.findAll(pageable);
    }

    public List<Appointment> findAll(){
        return appointmentRepository.findAll();
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public Page<Appointment> findAllFromHospitalByPage(Hospital hospital, Pageable pageRequest){
        return appointmentRepository.findAllByHospital(hospital, pageRequest);
    }

    public Page<Appointment> findAllAvailableFromHospitalByPage(Hospital hospital, Pageable pageRequest){
        return appointmentRepository.findAllByHospitalAndState(hospital, AppointmentState.AVAILABLE, pageRequest);
    }

    public Page<Appointment> findAllCitizenAppointments(Citizen citizen, Collection<AppointmentState> states, Pageable pageRequest){
        return appointmentRepository.findAllByCitizen(citizen, states, pageRequest);
    }

    public Page<Appointment> findAllByHospitalAndStateIn(Hospital hospital, Collection<AppointmentState> states, Pageable pageRequest){
        return appointmentRepository.findAllByHospitalAndStateIn(hospital, states, pageRequest);
    }

    public void delete(Long id){
        log.info("Deleting appointment with id " + id + ", total count: " + appointmentRepository.count());
        appointmentRepository.deleteById(id);
        log.info("Probably deleted appointment with id " + id + ", total count: " + appointmentRepository.count());
    }

    public Appointment save(Appointment appointment) {
        Appointment newAppointment = appointmentRepository.save(appointment);
        log.error("Appointment was saved - " + newAppointment);
        return appointmentRepository.save(newAppointment);
    }

    public Integer createAppointments(Hospital hospital, List<VaccineDto> vaccinesDto){
        int added = 0;
        for(VaccineDto vaccine : vaccinesDto){
            try {
                createAppointment(hospital, vaccine);
                added++;
            } catch(VaccineAlreadyExistsException ex){
                log.error(ex.getMessage());
            }
        }
        return added;
    }

    public Appointment createAppointment(Hospital hospital, VaccineDto vaccine){
        log.info("Assigning vaccine with code " + vaccine.getCode() + " from company " + vaccine.getCompanyName() + " to appointment");
        if(vaccineService.checkIfExistsByCode(vaccine.getCode())){
            throw new VaccineAlreadyExistsException("Vaccine with code " + vaccine.getCode() + " already exists");
        }
        Doctor doctor = getAppropriateDoctor(hospital);
        log.info("Choose doctor with id " + doctor.getId() + " and next appointment date " + doctor.getNextAppointmentDate() + " to vaccine " + vaccine);
        LocalDateTime appointmentDate = doctor.getNextAppointmentDate();
        doctor.setNextAppointmentDate(getAppointmentDate(appointmentDate));
        doctorService.save(doctor);
        Vaccine newVaccine = new Vaccine(vaccine.getCode(), companyService.findByName(vaccine.getCompanyName()), hospital);
        newVaccine.setState(VaccineState.ASSIGNED_TO_APPOINTMENT);
        Appointment appointment = new Appointment(appointmentDate, newVaccine, doctor);
        newVaccine.setAppointment(appointment);
        try {
            save(appointment);
        } catch (Exception e){
            log.error(e.toString());
            log.error("Vaccine with code " + vaccine.getCode() + " already exists, not added");
        }
        return appointment;
    }

    public ResponseEntity<Appointment> markAppointmentAsNotMade(Long id){
        User user = userService.getUserFromContext();
        Appointment appointment = getAppointmentByIdOrThrowException(id);
        log.info("Hospital " + user.getHospital() + " wants to mark appointment with id " + id + " as not made");
        if(appointment.getHospital().equals(user.getHospital())) {
            VaccineDto vaccine = new VaccineDto(appointment.getVaccine().getCode(), appointment.getVaccine().getCompany().getName());
            delete(id);
            log.info("Marked appointment " + appointment + " as not made");
            Appointment newAppointment = createAppointment(userService.getUserFromContext().getHospital(), vaccine);
            log.info("Created new appointment instead of this which was not made - " + newAppointment);
            return new ResponseEntity<>(newAppointment, HttpStatus.NO_CONTENT);
        }
        else{
            log.info("Hospital " + user.getCitizen() + " wanted to mark appointment with id " + id + " as made, but it is not it appointment!");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<Appointment> markAppointmentAsMade(Long id){
        User user = userService.getUserFromContext();
        Appointment appointment = getAppointmentByIdOrThrowException(id);
        log.info("Hospital " + user.getCitizen() + " wants to mark appointment with id " + id + " as made");
        if(appointment.getHospital().equals(user.getHospital())) {
            appointment.setState(AppointmentState.MADE);
            appointment.getVaccine().setState(VaccineState.GIVEN);
            log.info("Marked appointment " + appointment + " as made");
            save(appointment);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        else{
            log.info("Hospital " + user.getCitizen() + " wanted to mark appointment with id " + id + " as made, but it is not it appointment!");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<Appointment> enrollForTheAppointment(Long id){
        User user = userService.getUserFromContext();
        log.info("User " + user.getCitizen() + " wants to enroll appointment with id " + id);
        Appointment appointment = getAppointmentByIdOrThrowException(id);
        appointment.setState(AppointmentState.ASSIGNED);
        appointment.getVaccine().setState(VaccineState.ASSIGNED_TO_CITIZEN);
        appointment.setCitizen(user.getCitizen());
        save(appointment);
        log.info("User " + user.getCitizen() + " is enrolled appointment " + appointment);
        emailService.sendVisitConfirmation(user.getCitizen().getEmail(), parseDate(appointment.getDate()),
                EmailSubject.REGISTRATION_FOR_VACCINATION, user.getCitizen().getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    public ResponseEntity<Appointment> cancelAppointmentByCitizen(Long id){
        User user = userService.getUserFromContext();
        Appointment appointment = getAppointmentByIdOrThrowException(id);
        log.info("User " + user.getCitizen() + " wants to cancel appointment with id " + id);
        if(appointment.getCitizen().equals(user.getCitizen())) {
            appointment.setState(AppointmentState.AVAILABLE);
            appointment.getVaccine().setState(VaccineState.AVAILABLE);
            save(appointment);
            log.info("User " + user.getCitizen() + " cancelled appointment with id " + id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        else{
            log.info("User " + user.getCitizen() + " wants to cancel not his appointment with id " + id + ". His appointments - " + user.getCitizen().getAppointments());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<Appointment> getAppointmentById(Long id){
        Appointment appointment = getAppointmentByIdOrThrowException(id);
        return new ResponseEntity<>(appointment, HttpStatus.OK);
    }

    private Appointment getAppointmentByIdOrThrowException(Long id){
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "appointment"));
    }

    private Doctor getAppropriateDoctor(Hospital hospital){
        List<Doctor> doctors = new ArrayList<>(hospital.getDoctors());
        doctors.sort(Comparator.comparing(Doctor::getNextAppointmentDate));
        return doctors.get(0);
    }

    private LocalDateTime getAppointmentDate(LocalDateTime lastAppointmentDate){
        LocalDateTime appointmentDate;
        if (lastAppointmentDate.getHour() >= 15) {
            appointmentDate = getAvailableInNextDay(lastAppointmentDate);
        } else if(lastAppointmentDate.getHour() < 7){
            appointmentDate = LocalDateTime.of(lastAppointmentDate.getYear(),
                    lastAppointmentDate.getMonth(), lastAppointmentDate.getDayOfMonth(), 7, 0, 0);
        } else {
            appointmentDate = lastAppointmentDate.plusMinutes(5);
        }
        return appointmentDate;
    }

    private LocalDateTime getAvailableInNextDay(LocalDateTime last){
        LocalDateTime localDateTime = LocalDateTime.of(last.getYear(),
                last.getMonth(), last.getDayOfMonth(), 7, 0, 0);
        if(last.getDayOfWeek() == DayOfWeek.FRIDAY){
            localDateTime = localDateTime.plusDays(3);
        } else{
            localDateTime = localDateTime.plusDays(1);
        }

        return localDateTime;
    }

    private String parseDate(LocalDateTime time) {
        return String.format("%d.%d.%d %d:%d", time.getDayOfMonth(), time.getMonthValue(), time.getYear(),
                time.getHour(), time.getMinute());
    }
}
