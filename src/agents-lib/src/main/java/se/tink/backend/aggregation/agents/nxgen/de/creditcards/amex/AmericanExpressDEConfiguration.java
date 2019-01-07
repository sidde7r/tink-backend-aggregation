package se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex;

import se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex.fetcher.rpc.TimelineDERequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.core.Amount;

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
    public Amount toAmount(Double value) {
        // When the amount is 0.0 and we try to switch sign we end up with -0.0 what we would like
        // to avoid
        Amount amount = Amount.inEUR(value);
        if (amount.isZero()) {
            return amount;
        }
        // We are switching sign as Amex app shows values inversely to our standard
        return amount.negate();
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
