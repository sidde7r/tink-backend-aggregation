package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationDataEntity {
    private List<String> authenticationData;
    private String idAuthenticationData;

    AuthenticationDataEntity(String password, String dataId) {
        authenticationData = Collections.singletonList(password);
        this.idAuthenticationData = dataId;
    }
}
