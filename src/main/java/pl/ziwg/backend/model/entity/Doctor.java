package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "doctor")
@Getter
@ToString
@Setter
@NoArgsConstructor
public class Doctor {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long Id;

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "hospital", referencedColumnName = "id")
    private Hospital hospital;

    @JsonIgnore
    @OneToMany(mappedBy="doctor")
    private Set<Appointment> appointments;



}
