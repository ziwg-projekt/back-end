package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.ziwg.backend.model.enumerates.AppointmentState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "appointment")
@Getter
@ToString
@Setter
@NoArgsConstructor
public class Appointment {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long Id;

    @NotNull
    @JsonFormat(pattern="yyyy-MM-dd HH:mm", timezone = "Europe/Warsaw")
    private LocalDateTime date;

    @NotNull
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vaccine")
    private Vaccine vaccine;

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "citizen", referencedColumnName = "pesel", nullable = false)
    private Citizen citizen;

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "doctor", referencedColumnName = "id", nullable = false)
    private Doctor doctor;

    private AppointmentState state;

}
