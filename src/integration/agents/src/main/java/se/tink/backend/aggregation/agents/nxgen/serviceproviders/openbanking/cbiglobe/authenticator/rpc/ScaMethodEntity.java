package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class ScaMethodEntity {

    private String name;

    private String authenticationType;

    private String authenticationMethodId;

    public String getName() {
        return name;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }

    public String toString() {
        return authenticationType;
    }
}
