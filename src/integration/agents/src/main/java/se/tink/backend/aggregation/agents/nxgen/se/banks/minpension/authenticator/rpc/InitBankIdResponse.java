package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class InitBankIdResponse {
    private String autoStartToken;
    private String orderRef;
}
