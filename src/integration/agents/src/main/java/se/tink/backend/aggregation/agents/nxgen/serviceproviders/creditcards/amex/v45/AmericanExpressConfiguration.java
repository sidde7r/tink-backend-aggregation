package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineRequest;
import se.tink.libraries.amount.Amount;

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
