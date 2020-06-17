package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.entities.RequestComponent;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DeviceIdentification implements RequestComponent {

    @JsonProperty("APPLICATION_VERSION")
    private String APPLICATION_VERSION = "10.5.1";

    @JsonProperty("OS_NAME")
    private String OS_NAME = "iOS";

    @JsonProperty("MODEL")
    private String MODEL = "API_VERSION=2";

    @JsonProperty("MANUFACTURER")
    private String MANUFACTURER = "Apple";

    @JsonProperty("OS_VERSION")
    private String OS_VERSION = "13.3.1";

    @JsonProperty("APPLICATION_NAME")
    private String APPLICATION_NAME = "RFO";
}
