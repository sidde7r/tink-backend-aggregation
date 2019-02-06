package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationDataItemEntity {
    private List<String> authenticationData = new ArrayList<>();
    private String idAuthenticationData;

    public AuthenticationDataItemEntity(String password) {
        this.idAuthenticationData = "password";
        this.authenticationData.add(password);
    }
}
