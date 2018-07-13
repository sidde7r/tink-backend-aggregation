package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionEntity {
    private int status;

    public int getStatus() {
        return status;
    }
}
