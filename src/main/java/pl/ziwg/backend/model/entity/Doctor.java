package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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
        LocalDateTime now = LocalDateTime.now();
        if (now.getHour() < 7) {
            this.nextAppointmentDate = LocalDateTime.of(now.getYear(),
                    now.getMonth(), now.getDayOfMonth(), 7, 0, 0);
        } else {
            this.nextAppointmentDate = getAvailableInNextDay(now);
        }
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


    @Override
    public String toString() {
        return "Doctor{" +
                "id=" + id +
                ", nextAppointmentDate=" + nextAppointmentDate +
                '}';
    }
}
