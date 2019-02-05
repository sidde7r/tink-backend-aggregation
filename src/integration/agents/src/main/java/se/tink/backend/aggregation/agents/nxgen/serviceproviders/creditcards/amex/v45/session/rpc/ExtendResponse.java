package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.session.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExtendResponse {
    private SessionEntity extendSession;

    public SessionEntity getExtendSession() {
        return extendSession;
    }
}
