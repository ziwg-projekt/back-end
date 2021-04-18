package pl.ziwg.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.ziwg.backend.dto.AppointmentDto;
import pl.ziwg.backend.exception.ResourceNotFoundException;
import pl.ziwg.backend.model.entity.Appointment;
import pl.ziwg.backend.model.entity.Citizen;
import pl.ziwg.backend.model.entity.Doctor;
import pl.ziwg.backend.model.entity.Vaccine;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import pl.ziwg.backend.model.repository.AppointmentRepository;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {
    private AppointmentRepository appointmentRepository;
    private VaccineService vaccineService;
    private CitizenService citizenService;
    private DoctorService doctorService;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, VaccineService vaccineService,
                              CitizenService citizenService, DoctorService doctorService) {
        this.appointmentRepository = appointmentRepository;
        this.vaccineService = vaccineService;
        this.citizenService = citizenService;
        this.doctorService = doctorService;
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

    public Appointment save(Appointment appointment) {
        return appointmentRepository.save(appointment);
    }

    public Appointment saveDto(final AppointmentDto appointmentDto) {
        appointmentDto.setState(AppointmentState.CONFIRMED);
        final Vaccine vaccine = vaccineService.findByCode(appointmentDto.getVaccineCode())
                .orElseThrow(() -> new ResourceNotFoundException(appointmentDto.getVaccineCode(), "vaccine"));
        final Citizen citizen = citizenService.findByPesel(appointmentDto.getCitizenPesel())
                .orElseThrow(() -> new ResourceNotFoundException(appointmentDto.getCitizenPesel(), "pesel"));
        final Doctor doctor = doctorService.findById(appointmentDto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException(appointmentDto.getDoctorId(), "doctorId"));

        final Appointment appointment = new Appointment();
        appointment.setState(appointmentDto.getState());
        appointment.setVaccine(vaccine);
        appointment.setCitizen(citizen);
        appointment.setDoctor(doctor);
        appointment.setDate(appointmentDto.getDate());

        return appointmentRepository.save(appointment);
    }

}
