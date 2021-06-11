package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.entities.PsuDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeRequest {
    private String authenticationMethodId;
    private String clientID;
    private PsuDataEntity psuData;
    private String redirectUri;
    private String scope;

    public AuthorizeRequest(
            String clientID,
            String redirectUri,
            String bankId,
            String personalId,
            String authenticationMethodId,
            String scope) {
        this.clientID = clientID;
        this.redirectUri = redirectUri;
        psuData = new PsuDataEntity(bankId, personalId);
        this.scope = scope;
        this.authenticationMethodId = authenticationMethodId;
    }

    public AuthorizeRequest() {}
}
