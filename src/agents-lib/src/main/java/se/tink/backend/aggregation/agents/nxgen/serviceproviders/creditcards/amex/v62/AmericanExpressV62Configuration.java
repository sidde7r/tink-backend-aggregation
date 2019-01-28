package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.libraries.amount.Amount;

public interface AmericanExpressV62Configuration {

    public String getLocale();

    public String getAppId();
    public String getUserAgent();


    // == Credit Card Account ==
    public String getBankId(CardEntity cardEntity);

    public Amount toAmount(Double amount);

    // == Timeline Api ==
    public TimelineRequest createTimelineRequest(Integer cardIndex);

}
