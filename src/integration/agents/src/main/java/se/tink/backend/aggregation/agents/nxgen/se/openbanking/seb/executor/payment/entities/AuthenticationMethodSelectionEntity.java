package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationMethodSelectionEntity {
    String href;

    public boolean hasMethodSelectionEntity() {
        return href != null;
    }
}
