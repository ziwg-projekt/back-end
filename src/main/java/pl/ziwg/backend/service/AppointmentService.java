package pl.ziwg.backend.service;

import com.github.javafaker.App;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.BackendApplication;
import pl.ziwg.backend.dto.AppointmentDto;
import pl.ziwg.backend.dto.VaccineDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.model.enumerates.VaccineState;
import pl.ziwg.backend.model.repository.AppointmentRepository;

import javax.persistence.EntityExistsException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppointmentService {
    private AppointmentRepository appointmentRepository;
    private VaccineService vaccineService;
    private CitizenService citizenService;
    private DoctorService doctorService;
    private CompanyService companyService;
    protected static final Logger log = Logger.getLogger(BackendApplication.class);

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, VaccineService vaccineService,
                              CitizenService citizenService, DoctorService doctorService, CompanyService companyService) {
        this.appointmentRepository = appointmentRepository;
        this.vaccineService = vaccineService;
        this.citizenService = citizenService;
        this.doctorService = doctorService;
        this.companyService = companyService;
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

    public List<Appointment> findAllCitizenAppointments(Citizen citizen){
        return appointmentRepository.findAllByCitizen(citizen);
    }

    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public List<VaccineDto> createAppointments(Hospital hospital, List<VaccineDto> vaccinesDto){
        for(VaccineDto vaccine : vaccinesDto){
            log.info("Assigning vaccine with code " + vaccine.getCode() + " from company " + vaccine.getCompanyName() + " to appointment");
            List<Doctor> doctors = new ArrayList<>(hospital.getDoctors());
            doctors.sort(Comparator.comparing(Doctor::getLastAppointmentDate));
            Doctor doctor = doctors.get(0);
            LocalDateTime appointmentDate;
            LocalDateTime lastAppointmentDate = doctor.getLastAppointmentDate();
            if (lastAppointmentDate.getHour() >= 15) {
                appointmentDate = getAvailableInNextDay(lastAppointmentDate);
            } else {
                appointmentDate = lastAppointmentDate.plusMinutes(5);
            }
            doctor.setLastAppointmentDate(appointmentDate);
            doctorService.save(doctor);
            Vaccine newVaccine = new Vaccine(vaccine.getCode(), companyService.findByName(vaccine.getCompanyName()), hospital);
            newVaccine.setState(VaccineState.ASSIGNED_TO_APPOINTMENT);
            Appointment appointment = new Appointment(appointmentDate, newVaccine, doctor);
            newVaccine.setAppointment(appointment);
            try {
                save(appointment);
                log.info("Create appointment - " + appointment + " with vaccine " + vaccine.toString());
            } catch (Exception e){
                log.error("Vaccine with code " + vaccine.getCode() + " already exists, not added");
            }

        }

        return vaccinesDto;
    }

    public LocalDateTime getAvailableInNextDay(LocalDateTime last){
        LocalDateTime localDateTime = LocalDateTime.of(last.getYear(),
                last.getMonth(), last.getDayOfMonth(), 7, 0, 0);
        if(last.getDayOfWeek() == DayOfWeek.FRIDAY){
            localDateTime = localDateTime.plusDays(3);
        } else{
            localDateTime = localDateTime.plusDays(1);
        }

        return localDateTime;
    }
}
