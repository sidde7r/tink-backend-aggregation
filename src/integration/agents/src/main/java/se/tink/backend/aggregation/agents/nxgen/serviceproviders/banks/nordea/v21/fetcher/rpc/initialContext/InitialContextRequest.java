package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.rpc.initialContext;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class InitialContextRequest extends HttpRequestImpl {
    public InitialContextRequest() {
        super(HttpMethod.GET, NordeaV21Constants.Url.INITIAL_CONTEXT.get());
    }
}
