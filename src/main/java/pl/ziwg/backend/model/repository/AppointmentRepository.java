package pl.ziwg.backend.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.ziwg.backend.model.entity.*;
import pl.ziwg.backend.model.enumerates.AppointmentState;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Page<Appointment> findAll(Pageable pageable);
    Page<Appointment> findAllByHospital(Hospital hospital, Pageable pageable);
    List<Appointment> findAllByCitizen(Citizen citizen);
    Page<Appointment> findAllByHospitalAndState(Hospital hospital, AppointmentState state, Pageable pageRequest);
    Page<Appointment> findAllByHospitalAndStateIn(Hospital hospital, Collection<AppointmentState> states, Pageable pageRequest);
}
