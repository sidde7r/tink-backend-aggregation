package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaMethodEntity {
    private String authenticationType;
    private String authenticationMethodId;

    @JsonIgnore
    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }
}
