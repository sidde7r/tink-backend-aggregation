package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.rpc.initialContext;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class InitialContextRequest extends HttpRequestImpl {
    public InitialContextRequest() {
        super(HttpMethod.GET, NordeaV17Constants.Url.INITIAL_CONTEXT.get());
    }
}
