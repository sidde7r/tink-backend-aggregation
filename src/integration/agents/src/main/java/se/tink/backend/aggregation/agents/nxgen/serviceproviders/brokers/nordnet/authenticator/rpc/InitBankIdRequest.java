package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class InitBankIdRequest {
    @JsonProperty("User-Agent")
    private String userAgent;
}
