package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.ziwg.backend.model.enumerates.VaccineState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "vaccine")
@Getter
@ToString
@Setter
@NoArgsConstructor
public class Vaccine implements Serializable {
    @NotNull
    @Id
    private String code;

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "company", referencedColumnName = "id", nullable = false)
    private Company company;

    @NotNull
    @ManyToOne()
    @JoinColumn(name = "hospital", referencedColumnName = "id", nullable = false)
    private Hospital hospital;

    private VaccineState state;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "vaccine")
    private Appointment appointment;

    public Vaccine(String code, Company company, Hospital hospital){
        this.code = code;
        this.company = company;
        this.hospital = hospital;
        this.state = VaccineState.AVAILABLE;
    }


}
