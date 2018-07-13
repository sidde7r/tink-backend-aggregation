package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.entities.AuthorizeAgreementResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizeAgreementResponse extends NordeaResponse {
    private AuthorizeAgreementResponseEntity authorizeAgreementResponse;

    public AuthorizeAgreementResponseEntity getAuthorizeAgreementResponse() {
        return authorizeAgreementResponse;
    }
}
