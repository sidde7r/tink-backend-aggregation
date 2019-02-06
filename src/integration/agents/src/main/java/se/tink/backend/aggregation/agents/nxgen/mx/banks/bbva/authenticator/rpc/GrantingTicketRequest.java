package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.AuthenticationEntity;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.entity.BackendUserRequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GrantingTicketRequest {
    private BackendUserRequestEntity backendUserRequest;
    private AuthenticationEntity authentication;

    public GrantingTicketRequest(String phonenumber, String password) {
        this.backendUserRequest = new BackendUserRequestEntity(phonenumber);
        this.authentication = new AuthenticationEntity(password, phonenumber);
    }
}
