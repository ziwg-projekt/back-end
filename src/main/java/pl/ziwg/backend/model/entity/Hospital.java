package pl.ziwg.backend.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.apache.tomcat.jni.Local;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "hospital")
@NoArgsConstructor
@Getter
@Setter
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    private String name;

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name = "address", referencedColumnName="Id")
    @NotNull
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy="hospital", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Vaccine> vaccines;

    @JsonIgnore
    @OneToMany(mappedBy="hospital", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Citizen> citizens;

    @JsonIgnore
    @OneToMany(mappedBy="hospital", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<Doctor> doctors;

    @JsonIgnore
    @OneToOne()
    private User user;


    public Hospital(String name, Address address){
        this.name = name;
        this.address = address;
    }

    @Override
    public String toString() {
        return "Hospital{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", address=" + address +
                '}';
    }
}
