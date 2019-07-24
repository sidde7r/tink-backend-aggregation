package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.DeviceIdentification;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.HardwareInformation;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.InitResult;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
class VODB {
    @JsonProperty("DEVID01")
    public DeviceIdentification deviceIdentification;

    @JsonProperty("HWINFO01")
    public HardwareInformation hardwareInformation;

    // User info returned after activation, also sent as null now and then
    @JsonProperty("USRINF01")
    public UserInformation userInformation;

    @JsonProperty("RESULTO01")
    public InitResult initResult;
}
