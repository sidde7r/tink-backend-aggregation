package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.investment.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class CustodyAccountsRequest extends HttpRequestImpl {
    public CustodyAccountsRequest() {
        super(HttpMethod.GET, NordeaV21Constants.Url.CUSTODY_ACCOUNTS.get());
    }
}
