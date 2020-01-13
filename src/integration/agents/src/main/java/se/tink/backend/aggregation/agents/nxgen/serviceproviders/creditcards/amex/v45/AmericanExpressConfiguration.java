package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.libraries.amount.Amount;

public interface AmericanExpressConfiguration extends ClientConfiguration {

    String getAppId();

    String getUserAgent();

    String getFace();

    String getLocale();

    String getClientVersion();

    // == Credit Card Account ==
    String getBankId(CardEntity cardEntity);

    Amount toAmount(Double amount);

    // == Timeline Api ==
    TimelineRequest createTimelineRequest(Integer cardIndex);
}
