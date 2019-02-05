package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class VerifyCustomerResponse {
    private boolean valid;

    public boolean isValid() {
        return valid;
    }
}
