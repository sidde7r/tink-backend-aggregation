package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationResponseEntity {
    private List<Object> authenticationData;
    private String authenticationState;

    public List<Object> getAuthenticationData() {
        return authenticationData;
    }

    public String getAuthenticationState() {
        return authenticationState;
    }
}
