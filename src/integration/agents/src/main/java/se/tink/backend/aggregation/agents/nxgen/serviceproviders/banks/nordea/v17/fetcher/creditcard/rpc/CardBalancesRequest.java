package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants.UrlParameter;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class CardBalancesRequest extends HttpRequestImpl {
    public CardBalancesRequest(String cardNumber) {
        super(HttpMethod.GET, Url.CARDS_BALANCES.queryParam(UrlParameter.CARD_BALANCES_PARAM, cardNumber));
    }
}
