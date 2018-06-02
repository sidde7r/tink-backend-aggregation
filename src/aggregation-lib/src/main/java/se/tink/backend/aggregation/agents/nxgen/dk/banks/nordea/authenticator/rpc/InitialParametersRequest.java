package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class InitialParametersRequest extends HttpRequestImpl {
    public InitialParametersRequest(InitialParametersRequestBody body) {
        super(HttpMethod.POST, NordeaDkConstants.Url.INITIAL_PARAMETERS.get(), body);
    }
}
