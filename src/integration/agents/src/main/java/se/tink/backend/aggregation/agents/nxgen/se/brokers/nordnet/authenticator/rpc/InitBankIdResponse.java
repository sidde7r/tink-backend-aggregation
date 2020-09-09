package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class InitBankIdResponse {
    @JsonProperty("autostart_token")
    private String autostartToken;

    @JsonProperty("order_ref")
    private String orderRef;
}
