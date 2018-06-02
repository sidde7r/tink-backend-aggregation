package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.initialContext;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class InitialContextRequest extends HttpRequestImpl {
    public InitialContextRequest() {
        super(HttpMethod.GET, NordeaV20Constants.Url.INITIAL_CONTEXT.get());
    }
}
