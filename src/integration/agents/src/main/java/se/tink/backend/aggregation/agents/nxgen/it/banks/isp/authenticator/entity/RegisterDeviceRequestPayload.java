package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterDeviceRequestPayload {

    @JsonProperty("nomeDispositivo")
    private String deviceName = "iPhone 7";

    @JsonProperty("operazione")
    private String operation = "S";

    private boolean sendEmail = false;
}
