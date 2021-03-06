package pl.ziwg.backend.externalapi.opencagedata.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OpencageGeometry
{
    @JsonProperty("lat")
    private Float latitude;

    @JsonProperty("lng")
    private Float longitude;

    @Override
    public String toString() {
        return "OpencageGeometry{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
