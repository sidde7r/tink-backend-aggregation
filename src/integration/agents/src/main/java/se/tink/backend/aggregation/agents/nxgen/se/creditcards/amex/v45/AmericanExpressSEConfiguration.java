package se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v45;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v45.fetcher.rpc.TimelineSERequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineRequest;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressSEConfiguration implements AmericanExpressConfiguration {
    @Override
    public String getAppId() {
        return AmericanExpressSEConstants.HeaderValues.APP_ID;
    }

    @Override
    public String getUserAgent() {
        return AmericanExpressSEConstants.HeaderValues.USER_AGENT;
    }

    @Override
    public String getFace() {
        return AmericanExpressSEConstants.HeaderValues.FACE;
    }

    @Override
    public String getLocale() {
        return AmericanExpressSEConstants.BodyValue.LOCALE;
    }

    @Override
    public String getClientVersion() {
        return AmericanExpressSEConstants.BodyValue.CLIENT_VERSION;
    }

    @Override
    public String getBankId(CardEntity cardEntity) {
        return cardEntity.getCardNumberDisplay();
    }

    @Override
    public Amount toAmount(Double amount) {
        return Amount.inSEK(amount);
    }

    @Override
    public TimelineRequest createTimelineRequest(Integer cardIndex) {
        TimelineSERequest request = new TimelineSERequest();
        request.setTimeZone(AmericanExpressConstants.RequestValue.TIME_ZONE);
        request.setTimeZoneOffset(AmericanExpressConstants.RequestValue.TIME_ZONE_OFFSET);
        request.setSortedIndex(cardIndex);
        request.setLocalTime(new ThreadSafeDateFormat("MM-dd-YYYY'T'HH:mm:ss").format(new Date()));
        request.setPendingChargeEnabled(true);
        request.setCmlEnabled(true);
        request.setTimestamp(Long.toString(System.currentTimeMillis()));
        return request;
    }
}
