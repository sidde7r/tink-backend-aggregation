package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.rpc;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.UrlParameter;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class CreditCardTransactionsRequest extends HttpRequestImpl {
    public CreditCardTransactionsRequest(String cardNumber, String invoicePeriod, String continueKey) {
        super(HttpMethod.GET, Url.TRANSACTIONS.queryParam(UrlParameter.CARD_NUMBER, cardNumber)
                .queryParam(UrlParameter.CREDIT_CONTINUE_KEY, Strings.nullToEmpty(continueKey))
                .queryParam(UrlParameter.INVOICE_PERIOD, Strings.nullToEmpty(invoicePeriod)));
    }
}
