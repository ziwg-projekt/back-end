package pl.ziwg.backend.jsonbody.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.ziwg.backend.notificator.CommunicationChannelType;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString
public class RegistrationRequestBody {
    @NotNull
    private String pesel;

    @JsonProperty("communication_channel_type")
    @NotNull
    private CommunicationChannelType communicationChannelType;

    public RegistrationRequestBody(String pesel, int communicationChannelType){
        this.pesel = pesel;
        this.communicationChannelType = CommunicationChannelType.values()[communicationChannelType];
    }
}
