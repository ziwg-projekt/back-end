package pl.ziwg.backend.externalapi.opencagedata.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OpencageResult
{
    @JsonProperty("confidence")
    private int confidence;

    @JsonProperty("formatted")
    private String formatted;

    @JsonProperty("components")
    private OpencageComponent components;

    @JsonProperty("geometry")
    private OpencageGeometry geometry;

    @Override
    public String toString() {
        return "OpencageResult{" +
                "confidence=" + confidence +
                ", formatted='" + formatted + '\'' +
                ", components=" + components +
                ", geometry=" + geometry +
                '}';
    }
}
