package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.RequestComponent;

public class DeviceIdentification implements RequestComponent {

    @JsonProperty("APPLICATION_VERSION")
    public String APPLICATION_VERSION = "10.0.1";

    @JsonProperty("OS_NAME")
    public String OS_NAME = "iOS";

    @JsonProperty("MODEL")
    public String MODEL = "API_VERSION=2";

    @JsonProperty("MANUFACTURER")
    public String MANUFACTURER = "Apple";

    @JsonProperty("OS_VERSION")
    public String OS_VERSION = "12.4";

    @JsonProperty("APPLICATION_NAME")
    public String APPLICATION_NAME = "MASP";
}
