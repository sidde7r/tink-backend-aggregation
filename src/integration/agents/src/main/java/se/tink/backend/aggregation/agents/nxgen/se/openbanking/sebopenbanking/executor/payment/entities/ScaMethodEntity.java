package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sebopenbanking.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodEntity {
    private String authenticationType;
    private String authenticationMethodId;
    private String name;
    private String explanation;

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }
}
