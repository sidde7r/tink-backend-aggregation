package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.loan.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.UrlParameter;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

public class LoanDetailsRequest extends HttpRequestImpl {
    public LoanDetailsRequest(String accountId) {
        super(HttpMethod.GET, Url.LOANS.parameter(UrlParameter.ACCOUNT_ID, accountId));
    }
}
