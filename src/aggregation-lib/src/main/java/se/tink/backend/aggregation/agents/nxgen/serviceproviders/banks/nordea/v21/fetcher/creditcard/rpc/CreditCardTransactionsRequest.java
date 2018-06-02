package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.UrlParameter;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class CreditCardTransactionsRequest extends HttpRequestImpl {
    public CreditCardTransactionsRequest(String cardNumber, String continueKey) {
        super(HttpMethod.GET, Url.TRANSACTIONS.queryParam(UrlParameter.CARD_NUMBER, cardNumber)
                .queryParam(UrlParameter.CONTINUATION_KEY, continueKey)
                .queryParam("ownTransactionsOnly", "false"));
    }
}
