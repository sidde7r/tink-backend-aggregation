package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AuthorizeAgreementDetails;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeAgreementRequestBody {
    private AuthorizeAgreementDetails authorizeAgreementRequest;

    public AuthorizeAgreementRequestBody setAuthorizeAgreementRequest(
            AuthorizeAgreementDetails authorizeAgreementRequest) {
        this.authorizeAgreementRequest = authorizeAgreementRequest;
        return this;
    }
}
