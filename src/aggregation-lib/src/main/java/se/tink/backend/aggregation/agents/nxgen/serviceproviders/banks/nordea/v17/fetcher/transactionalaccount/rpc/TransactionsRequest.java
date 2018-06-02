package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.rpc;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.UrlParameter;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class TransactionsRequest extends HttpRequestImpl {
    public TransactionsRequest(String accountId, String continueKey) {
        super(HttpMethod.GET, Url.TRANSACTIONS.queryParam(UrlParameter.ACCOUNT_ID, accountId)
                .queryParam(UrlParameter.CONTINUE_KEY, Strings.nullToEmpty(continueKey)));
    }
}
