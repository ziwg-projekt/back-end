package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.ziwg.backend.model.enumerates.AppointmentState;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Entity
@Table(name = "appointment")
@Getter
@Setter
@NoArgsConstructor
public class Appointment {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long id;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING, timezone = "Europe/Warsaw")
    @ApiModelProperty(required = true, example = "2021-08-20 12:00")
    private LocalDateTime date;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "vaccine")
    private Vaccine vaccine;

    @ManyToOne()
    @JoinColumn(name = "citizen", referencedColumnName = "pesel")
    private Citizen citizen;

    @ManyToOne()
    @JoinColumn(name = "doctor", referencedColumnName = "id")
    private Doctor doctor;

    private AppointmentState state;

    @ManyToOne()
    private Hospital hospital;

    @JsonIgnore
    private boolean ifReminded;


    public Appointment(LocalDateTime date, Vaccine vaccine, Doctor doctor) {
        this.date = date;
        this.vaccine = vaccine;
        this.doctor = doctor;
        this.hospital = doctor.getHospital();
        this.state = AppointmentState.AVAILABLE;
        this.ifReminded = false;
    }


    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", date=" + date +
                ", citizen=" + citizen +
                ", doctor=" + doctor +
                ", state=" + state +
                '}';
    }
}
