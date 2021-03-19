package pl.ziwg.backend.externalapi.opencagedata.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class GeocodeResponse
{
    @JsonProperty("results")
    private List<OpencageResult> results;

    @JsonProperty("rate")
    private OpencageRate rate;

    @JsonProperty("status")
    private OpencageStatus status;

    @JsonProperty("total_results")
    private int totalResults;

    @Override
    public String toString() {
        return "GeocodeResponse{" +
                "results=" + results +
                ", rate=" + rate +
                ", status=" + status +
                ", totalResults=" + totalResults +
                '}';
    }
}
