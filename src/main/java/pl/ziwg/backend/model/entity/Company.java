package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.Set;

@Entity
@Table(name = "company")
@Getter
@ToString
@Setter
@NoArgsConstructor
public class Company {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private long Id;

    @NotEmpty
    private String name;

    @Column(length = 1000)
    private byte[] logoByte;

    @JsonIgnore
    @OneToMany(mappedBy="company")
    private Set<Vaccine> vaccines;

}
