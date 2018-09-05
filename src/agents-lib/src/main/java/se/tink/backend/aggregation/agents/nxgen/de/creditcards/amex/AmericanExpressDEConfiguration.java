package se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.creditcards.amex.fetcher.rpc.TimelineDERequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.fetcher.rpc.TimelineRequest;
import se.tink.backend.core.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressDEConfiguration implements AmericanExpressConfiguration {

    @Override
    public String getAppId() {
        return AmericanExpressDEConstants.HeaderValues.APP_ID;
    }

    @Override
    public String getUserAgent() {
        return AmericanExpressDEConstants.HeaderValues.USER_AGENT;
    }

    @Override
    public String getFace() {
        return AmericanExpressDEConstants.HeaderValues.FACE;
    }

    @Override
    public String getLocale() {
        return AmericanExpressDEConstants.BodyValue.LOCALE;
    }

    @Override
    public String getClientVersion() {
        return AmericanExpressDEConstants.BodyValue.CLIENT_VERSION;
    }

    @Override
    public String getBankId(CardEntity cardEntity) {
        return cardEntity.getCardNumberDisplay();
    }

    @Override
    public Amount toAmount(Double amount) {
        return Amount.inEUR(amount);
    }

    @Override
    public TimelineRequest createTimelineRequest(Integer cardIndex) {
        TimelineDERequest request = new TimelineDERequest();
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
