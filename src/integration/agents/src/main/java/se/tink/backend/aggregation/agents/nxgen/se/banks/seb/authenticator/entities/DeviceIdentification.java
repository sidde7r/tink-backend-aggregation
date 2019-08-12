package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.entities.RequestComponent;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceIdentification implements RequestComponent {

    @JsonProperty("APPLICATION_VERSION")
    private String APPLICATION_VERSION = "10.0.1";

    @JsonProperty("OS_NAME")
    private String OS_NAME = "iOS";

    @JsonProperty("MODEL")
    private String MODEL = "API_VERSION=2";

    @JsonProperty("MANUFACTURER")
    private String MANUFACTURER = "Apple";

    @JsonProperty("OS_VERSION")
    private String OS_VERSION = "12.4";

    @JsonProperty("APPLICATION_NAME")
    private String APPLICATION_NAME = "MASP";
}
