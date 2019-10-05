package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

public class CustodyAccountsRequest extends HttpRequestImpl {
    public CustodyAccountsRequest() {
        super(HttpMethod.GET, NordeaV20Constants.Url.CUSTODY_ACCOUNTS.get());
    }
}
