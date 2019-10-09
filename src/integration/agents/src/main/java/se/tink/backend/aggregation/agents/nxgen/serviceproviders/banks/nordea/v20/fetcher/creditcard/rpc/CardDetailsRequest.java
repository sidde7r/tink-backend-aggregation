package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.Url;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants.UrlParameter;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;

public class CardDetailsRequest extends HttpRequestImpl {
    public CardDetailsRequest(String cardNumber) {
        super(HttpMethod.GET, Url.CARDS.parameter(UrlParameter.CARD_NUMBER, cardNumber));
    }
}
