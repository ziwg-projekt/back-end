package pl.ziwg.backend.externalapi.opencagedata.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OpencageRate
{
    @JsonProperty("limit")
    private int limit;

    @JsonProperty("remaining")
    private int remaining;

    @JsonProperty("reset")
    private int reset;

    @Override
    public String toString() {
        return "OpencageRate{" +
                "limit=" + limit +
                ", remaining=" + remaining +
                ", reset=" + reset +
                '}';
    }
}
