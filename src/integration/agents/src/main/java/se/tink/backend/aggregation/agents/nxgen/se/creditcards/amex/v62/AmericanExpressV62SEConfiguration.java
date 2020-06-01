package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62.fetcher.rpc.TimelineSERequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;

public class AmericanExpressV62SEConfiguration implements AmericanExpressV62Configuration {

    @Override
    public String getLocale() {
        return AmericanExpressV62SEConstants.HeaderValues.LOCALE;
    }

    @Override
    public String getBankId(CardEntity cardEntity) {
        return cardEntity.getCardNumberDisplay();
    }

    @Override
    public String getCurrency() {
        return "SEK";
    }

    @Override
    public TimelineRequest createTimelineRequest(Integer cardIndex) {
        TimelineSERequest request = new TimelineSERequest();
        request.setCmlEnabled(true);
        request.setGoodsSvcOfferEnabled(false);
        request.setPayWithPointsEnabled(false);
        request.setPayYourWayEnabled(false);
        request.setPendingChargeEnabled(true);
        request.setPushEnabled(true);
        request.setSortedIndex(cardIndex);
        return request;
    }

    @Override
    public String getAppId() {
        return AmericanExpressV62SEConstants.HeaderValues.APP_ID;
    }

    @Override
    public String getInitVersion() {
        return AmericanExpressV62SEConstants.INIT_VERSION;
    }

    @Override
    public String getUserAgent() {
        return AmericanExpressV62SEConstants.HeaderValues.USER_AGENT;
    }

    @Override
    public String getAppVersion() {
        return AmericanExpressV62SEConstants.HeaderValues.APP_VERSION;
    }
}
