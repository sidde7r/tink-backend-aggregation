package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class AuthorizeAgreementRequest extends HttpRequestImpl {
    public AuthorizeAgreementRequest(AuthorizeAgreementRequestBody body) {
        super(HttpMethod.POST, NordeaDkConstants.Url.AGREEMENT_AUTHORIZATION.get(), body);
    }
}
