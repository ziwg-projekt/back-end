package pl.ziwg.backend.model.entity;


import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import pl.ziwg.backend.model.EntityToMapConverter;
import pl.ziwg.backend.model.enumerates.UserType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @NotBlank
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Citizen citizen;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Hospital hospital;

    private UserType userType;

    public User(String username, String password, Citizen citizen){
        this.username = username;
        this.password = password;
        this.citizen = citizen;
        this.citizen.setUser(this);
        this.userType = UserType.CITIZEN;
    }

    public User(String username, String password, Hospital hospital){
        this.username = username;
        this.password = password;
        this.hospital = hospital;
        this.hospital.setUser(this);
        this.userType = UserType.HOSPITAL;
    }


    public User(String username, String password, UserType userType){
        this.username = username;
        this.password = password;
        this.userType = userType;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", type='" + userType + '\'' +
                ", roles=" + roles +
                ", citizen=" + EntityToMapConverter.getRepresentationWithoutChosenFields(citizen, Arrays.asList("user")) +
                ", hospital=" + EntityToMapConverter.getRepresentationWithoutChosenFields(hospital, Arrays.asList("user")) +
                '}';
    }
}
