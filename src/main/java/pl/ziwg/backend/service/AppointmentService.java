package pl.ziwg.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.dto.HospitalEnrollDto;
import pl.ziwg.backend.dto.VaccineDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.exception.UserTypeException;
import pl.ziwg.backend.exception.VaccineAlreadyExistsException;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.model.enumerates.CitizenState;
import pl.ziwg.backend.model.enumerates.UserType;
import pl.ziwg.backend.model.enumerates.VaccineState;
import pl.ziwg.backend.model.repository.AppointmentRepository;
import pl.ziwg.backend.notificator.email.EmailSubject;

import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

import static pl.ziwg.backend.template.ReadFileUtils.parseDate;

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
    private SMSService smsService;
    protected static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, VaccineService vaccineService,
                              CitizenService citizenService, DoctorService doctorService, CompanyService companyService,
                              UserService userService, EmailService emailService, SMSService smsService) {
        this.appointmentRepository = appointmentRepository;
        this.vaccineService = vaccineService;
        this.citizenService = citizenService;
        this.doctorService = doctorService;
        this.companyService = companyService;
        this.userService = userService;
        this.emailService = emailService;
        this.smsService = smsService;
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
        log.info("Appointment was saved - " + newAppointment);
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
        Doctor doctor = getDoctorWithEarliestNextAppointmentDate(hospital);
        log.info("Choose doctor with id " + doctor.getId() + " and next appointment date " + doctor.getNextAppointmentDate() + " to vaccine " + vaccine);
        LocalDateTime appointmentDate = doctor.getNextAppointmentDate();
        doctor.setNextAppointmentDate(AppointmentDateService.getNextAppointmentDate(appointmentDate));
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
            Appointment newAppointment = createAppointmentInsteadOfTheOldOne(appointment, user.getHospital());
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
            appointment.getCitizen().setState(CitizenState.FINISHED);
            log.info("Marked appointment " + appointment + " as made");
            save(appointment);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        else{
            log.info("Hospital " + user.getCitizen() + " wanted to mark appointment with id " + id + " as made, but it is not it appointment!");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    public ResponseEntity<Appointment> enrollForTheAppointment(Long appointmentId){
        User user = userService.getUserFromContext();
        return enrollCitizenForTheAppointment(user.getCitizen(), getAppointmentByIdOrThrowException(appointmentId));
    }

    public ResponseEntity<Appointment> enrollForTheAppointment(Long appointmentId,
                                                               HospitalEnrollDto hospitalEnrollDto) {
        final Citizen citizen = citizenService.findByPesel(hospitalEnrollDto.getPesel())
                .orElseThrow(() -> new ResourceNotFoundException(hospitalEnrollDto.getPesel(), "pesel"));
        final User user = userService.findById(citizen.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException(citizen.getUser().getId(), "user"));
        if (UserType.CITIZEN != user.getUserType()) {
            throw new UserTypeException("Given user id must belong to the citizen");
        }
        final Appointment appointment = this.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException(appointmentId, "appointment"));

        return this.enrollCitizenForTheAppointment(citizen, appointment);
    }

    private void updateAppointmentAfterEnroll(Appointment appointment, Citizen citizen){
        appointment.setState(AppointmentState.ASSIGNED);
        appointment.getVaccine().setState(VaccineState.ASSIGNED_TO_CITIZEN);
        appointment.setCitizen(citizen);
        save(appointment);
    }

    public ResponseEntity<Appointment> enrollCitizenForTheAppointment(final Citizen citizen,
                                                                      final Appointment appointment) {
        log.info("Citizen " + citizen + " wants to enroll appointment with id " + appointment.getId());
        updateAppointmentAfterEnroll(appointment, citizen);
        log.info("Citizen " + citizen + " is enrolled appointment " + appointment);
        emailService.sendVisitConfirmation(citizen.getEmail(), parseDate(appointment.getDate()),
                EmailSubject.REGISTRATION_FOR_VACCINATION, citizen.getName());
        if (Objects.nonNull(citizen.getPhoneNumber())) {
            try {
                smsService.sendAppointmentConfirmation("+48".concat(citizen.getPhoneNumber()), appointment.getDate(),
                        citizen.getAddress(), appointment.getVaccine().getCompany());
            } catch (Exception e) {
                log.error("Given number is not verified");
            }
        }
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

    public Appointment createAppointmentInsteadOfTheOldOne(Appointment oldAppointment, Hospital hospital){
        VaccineDto vaccine = new VaccineDto(oldAppointment.getVaccine().getCode(), oldAppointment.getVaccine().getCompany().getName());
        delete(oldAppointment.getId());
        log.info("Delete appointment " + oldAppointment);
        Appointment newAppointment = createAppointment(hospital, vaccine);
        log.info("Created new appointment instead of this one which was deleted - " + newAppointment);
        return newAppointment;
    }

    public ResponseEntity<Appointment> getAppointmentById(Long id){
        Appointment appointment = getAppointmentByIdOrThrowException(id);
        return new ResponseEntity<>(appointment, HttpStatus.OK);
    }

    private Appointment getAppointmentByIdOrThrowException(Long id){
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id, "appointment"));
    }

    private Doctor getDoctorWithEarliestNextAppointmentDate(Hospital hospital){
        List<Doctor> doctors = new ArrayList<>(hospital.getDoctors());
        doctors.sort(Comparator.comparing(Doctor::getNextAppointmentDate));
        return doctors.get(0);
    }


}
