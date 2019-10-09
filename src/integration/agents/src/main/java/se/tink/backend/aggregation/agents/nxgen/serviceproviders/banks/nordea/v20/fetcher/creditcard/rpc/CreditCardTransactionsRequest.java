package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.UrlParameter;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

public class CreditCardTransactionsRequest extends HttpRequestImpl {
    public CreditCardTransactionsRequest(String cardNumber, String continueKey) {
        super(
                HttpMethod.GET,
                Url.TRANSACTIONS
                        .queryParam(UrlParameter.CARD_NUMBER, cardNumber)
                        .queryParam(UrlParameter.CONTINUATION_KEY, continueKey)
                        .queryParam("ownTransactionsOnly", "false"));
    }
}
