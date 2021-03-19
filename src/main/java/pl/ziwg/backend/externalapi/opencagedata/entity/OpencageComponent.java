package pl.ziwg.backend.externalapi.opencagedata.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OpencageComponent
{
    private String city;

    @JsonProperty("town")
    private String town;

    @JsonProperty("postcode")
    private String postcode;

    private String country;

    @JsonProperty("country_code")
    private String countryCode;

    private String county;

    private String state;

    @JsonProperty("state_district")
    private String stateDistrict;

    @Override
    public String toString() {
        return "OpencageComponent{" +
                "city='" + city + '\'' +
                ", town='" + town + '\'' +
                ", postcode='" + postcode + '\'' +
                ", country='" + country + '\'' +
                ", countryCode='" + countryCode + '\'' +
                ", county='" + county + '\'' +
                ", state='" + state + '\'' +
                ", stateDistrict='" + stateDistrict + '\'' +
                '}';
    }
}
