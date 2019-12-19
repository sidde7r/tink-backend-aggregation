package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.LoginParameter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationDataEntity {
    private List<String> authenticationData;
    private String idAuthenticationData;

    AuthenticationDataEntity(String password) {
        authenticationData = Collections.singletonList(password);
        this.idAuthenticationData = LoginParameter.AUTH_DATA_ID;
    }
}
