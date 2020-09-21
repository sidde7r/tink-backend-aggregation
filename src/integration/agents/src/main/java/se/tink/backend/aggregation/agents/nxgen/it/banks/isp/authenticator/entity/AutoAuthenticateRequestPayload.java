package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class AutoAuthenticateRequestPayload {

    @JsonProperty("deviceID")
    private String deviceId;

    @JsonProperty("ricordami")
    private String rememberMeCode;
}
