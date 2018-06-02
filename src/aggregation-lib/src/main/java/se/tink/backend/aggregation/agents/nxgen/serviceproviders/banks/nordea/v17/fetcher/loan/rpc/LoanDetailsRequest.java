package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.UrlParameter;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class LoanDetailsRequest extends HttpRequestImpl {
    public LoanDetailsRequest(String accountId) {
        super(HttpMethod.GET, Url.LOANS.parameter(UrlParameter.ACCOUNT_ID, accountId));
    }
}
