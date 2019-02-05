package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class CustodyAccountsRequest extends HttpRequestImpl {
    public CustodyAccountsRequest() {
        super(HttpMethod.GET, NordeaV17Constants.Url.CUSTODY_ACCOUNTS.get());
    }
}
