package pl.ziwg.backend.externalapi.opencagedata.entity;

import org.codehaus.jackson.annotate.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GeocodeResponse
{
    private List<OpencageResult> results;
    private OpencageRate rate;
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
