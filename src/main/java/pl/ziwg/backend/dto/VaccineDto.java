package pl.ziwg.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class VaccineDto {
    private String code;

    @JsonProperty(value="company_name")
    private String companyName;
}
