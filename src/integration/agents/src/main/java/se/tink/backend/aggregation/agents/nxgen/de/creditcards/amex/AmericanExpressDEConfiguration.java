package se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex;

import se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex.fetcher.rpc.TimelineDERequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;

public class AmericanExpressDEConfiguration implements AmericanExpressV62Configuration {

    @Override
    public String getAppId() {
        return AmericanExpressDEConstants.HeaderValues.APP_ID;
    }

    @Override
    public String getUserAgent() {
        return AmericanExpressDEConstants.HeaderValues.USER_AGENT;
    }

    @Override
    public String getBankId(CardEntity cardEntity) {
        return cardEntity.getCardNumberDisplay();
    }

    @Override
    public String getLocale() {
        return AmericanExpressDEConstants.BodyValue.LOCALE;
    }

    @Override
    public String getCurrency() {
        return "EUR";
    }

    @Override
    public TimelineRequest createTimelineRequest(Integer cardIndex) {
        TimelineDERequest request = new TimelineDERequest();
        request.setSortedIndex(String.valueOf(cardIndex));
        request.setPendingChargeEnabled(true);
        request.setCmlEnabled(true);
        request.setGoodsSvcOfferEnabled(true);
        request.setPayWithPointsEnabled(true);
        request.setPayYourWayEnabled(false);
        request.setPushEnabled(false);

        return request;
    }
}
