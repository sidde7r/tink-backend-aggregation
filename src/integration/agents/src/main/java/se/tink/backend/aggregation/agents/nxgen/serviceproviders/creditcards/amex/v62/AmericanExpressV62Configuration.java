package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;

public interface AmericanExpressV62Configuration {

    String getLocale();

    String getAppId();

    String getUserAgent();

    // == Credit Card Account ==
    String getBankId(CardEntity cardEntity);

    String getCurrency();

    // == Timeline Api ==
    TimelineRequest createTimelineRequest(Integer cardIndex);
}
