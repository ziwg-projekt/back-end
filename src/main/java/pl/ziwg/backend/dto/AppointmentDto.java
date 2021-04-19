package pl.ziwg.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.ziwg.backend.model.enumerates.AppointmentState;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AppointmentDto {
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", shape = JsonFormat.Shape.STRING, timezone = "Europe/Warsaw")
    @ApiModelProperty(required = true, example = "2021-08-20 12:00")
    private LocalDateTime date;

    @NotNull
    private String vaccineCode;

    @NotNull
    private String citizenPesel;

    @NotNull
    private long doctorId;

    @JsonIgnore
    private AppointmentState state;
}
