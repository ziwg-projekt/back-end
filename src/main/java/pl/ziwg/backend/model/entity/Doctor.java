package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.ziwg.backend.service.AppointmentDateService;
import pl.ziwg.backend.service.AppointmentService;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor
public class Doctor {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "hospital", referencedColumnName = "id")
    private Hospital hospital;

    @JsonIgnore
    @OneToMany(mappedBy="doctor", orphanRemoval = true)
    private Set<Appointment> appointments;

    @JsonIgnore
    private LocalDateTime nextAppointmentDate;

    public Doctor(Hospital hospital){
        this.hospital = hospital;
        this.nextAppointmentDate = AppointmentDateService.getNextValidAppointmentDateFromGivenDate(LocalDateTime.now());
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", nextAppointmentDate=" + nextAppointmentDate +
                '}';
    }
}
