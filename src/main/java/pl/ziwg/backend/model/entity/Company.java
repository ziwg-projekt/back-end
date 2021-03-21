package pl.ziwg.backend.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@Table(name = "vaccine")
@Getter
@ToString
@Setter
@NoArgsConstructor
public class Company {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @javax.persistence.Id
    private long Id;

    @NotEmpty
    private String name;

}
