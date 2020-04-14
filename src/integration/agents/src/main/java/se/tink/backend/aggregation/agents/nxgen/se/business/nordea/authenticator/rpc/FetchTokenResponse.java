package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities.AuthorizeAgreementResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc.NordeaSEResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchTokenResponse extends NordeaSEResponse {
    private AuthorizeAgreementResponseEntity authorizeAgreementResponse;

    public String getToken() {
        return authorizeAgreementResponse.getToken();
    }
}
