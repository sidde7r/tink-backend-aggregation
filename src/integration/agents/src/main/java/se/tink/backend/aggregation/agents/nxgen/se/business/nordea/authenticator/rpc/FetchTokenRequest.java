package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities.AuthorizeAgreementRequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTokenRequest {
    public AuthorizeAgreementRequestEntity authorizeAgreementRequest;

    public FetchTokenRequest(String id) {
        authorizeAgreementRequest = new AuthorizeAgreementRequestEntity(id);
    }
}
