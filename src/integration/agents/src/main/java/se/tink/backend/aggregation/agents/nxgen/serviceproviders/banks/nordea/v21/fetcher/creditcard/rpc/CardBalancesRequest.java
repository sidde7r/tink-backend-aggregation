package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class CardBalancesRequest extends HttpRequestImpl {
    public CardBalancesRequest(String accountId) {
        super(
                HttpMethod.GET,
                NordeaV21Constants.Url.CARD_BALANCES.queryParam(
                        NordeaV21Constants.UrlParameter.CARD_NUMBERS, accountId));
    }
}
