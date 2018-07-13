package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class NemIdAuthenticateUserRequest extends HttpRequestImpl {
    public NemIdAuthenticateUserRequest(NemIdAuthenticateUserRequestBody body) {
        super(HttpMethod.POST, NordeaDkConstants.Url.NEMID_LOGIN.get(), body);
    }
}
