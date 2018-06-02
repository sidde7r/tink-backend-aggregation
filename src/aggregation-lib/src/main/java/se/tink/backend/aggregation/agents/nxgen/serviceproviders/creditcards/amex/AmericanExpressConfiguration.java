package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.fetcher.rpc.TimelineRequest;
import se.tink.backend.core.Amount;

public interface AmericanExpressConfiguration {

    public String getAppId();

    public String getUserAgent();

    public String getFace();

    public String getLocale();

    public String getClientVersion();

    // == Credit Card Account ==
    public String getBankId(CardEntity cardEntity);

    public Amount toAmount(Double amount);

    // == Timeline Api ==
    public TimelineRequest createTimelineRequest(Integer cardIndex);

}
