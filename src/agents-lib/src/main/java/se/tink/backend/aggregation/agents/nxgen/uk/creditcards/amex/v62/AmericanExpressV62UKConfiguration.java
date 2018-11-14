package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62.fetcher.rpc.TimelineUKRequest;
import se.tink.backend.core.Amount;

public class AmericanExpressV62UKConfiguration implements AmericanExpressV62Configuration {

    @Override
    public String getLocale() {
        return AmericanExpressV62UKConstants.LOCALE;
    }

    @Override
    public String getBankId(CardEntity cardEntity) {
        return cardEntity.getCardNumberDisplay();
    }

    @Override
    public Amount toAmount(Double amount) {
        // When the amount is 0.0 and we try to switch sign we end up with -0.0 what we would like to avoid
        if (amount == 0.0d) {
            return Amount.inSEK(amount);
        }
        // We are switching sign as Amex app shows values inversely to our standard
        return Amount.inSEK(amount * -1d);
    }

    @Override
    public TimelineRequest createTimelineRequest(Integer cardIndex) {
        TimelineUKRequest request = new TimelineUKRequest();

        request.setSortedIndex(String.valueOf(cardIndex));
        request.setPendingChargeEnabled(true);
        request.setCmlEnabled(true);
        request.setGoodsSvcOfferEnabled(true);
        request.setPayWithPointsEnabled(true);
        request.setPayYourWayEnabled(false);
        request.setPushEnabled(false);

        return request;
    }

    @Override
    public String getAppId() {
        return AmericanExpressV62UKConstants.APP_ID;
    }

    @Override
    public String getUserAgent() {
        return AmericanExpressV62UKConstants.USER_AGENT;
    }
}
