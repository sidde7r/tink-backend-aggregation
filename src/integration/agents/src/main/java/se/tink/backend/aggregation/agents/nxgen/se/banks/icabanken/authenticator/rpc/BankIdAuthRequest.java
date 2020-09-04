package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class BankIdAuthRequest {
    private String orderRef;
}
