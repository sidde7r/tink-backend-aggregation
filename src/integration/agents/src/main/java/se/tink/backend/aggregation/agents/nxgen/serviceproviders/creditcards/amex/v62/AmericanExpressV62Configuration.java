package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public interface AmericanExpressV62Configuration extends ClientConfiguration {

    default String getGitSha() {
        return null;
    }

    String getLocale();

    String getAppId();

    String getUserAgent();
    // == Credit Card Account ==

    String getBankId(CardEntity cardEntity);

    String getCurrency();
    // == Timeline Api ==

    TimelineRequest createTimelineRequest(Integer cardIndex);

    String getAppVersion();
}
