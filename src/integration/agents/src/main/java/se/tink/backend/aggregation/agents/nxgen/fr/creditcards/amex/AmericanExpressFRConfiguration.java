package se.tink.backend.aggregation.agents.nxgen.fr.creditcards.amex;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fr.creditcards.amex.fetcher.rpc.TimelineFRRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.AmericanExpressConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v45.authenticator.entities.CardEntity;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressFRConfiguration implements AmericanExpressConfiguration {

    @Override
    public String getAppId() {
        return AmericanExpressFRConstants.HeaderValues.APP_ID;
    }

    @Override
    public String getUserAgent() {
        return AmericanExpressFRConstants.HeaderValues.USER_AGENT;
    }

    @Override
    public String getFace() {
        return AmericanExpressFRConstants.HeaderValues.FACE;
    }

    @Override
    public String getLocale() {
        return AmericanExpressFRConstants.BodyValues.LOCALE;
    }

    @Override
    public String getClientVersion() {
        return AmericanExpressFRConstants.BodyValues.CLIENT_VERSION;
    }

    @Override
    public String getBankId(CardEntity cardEntity) {
        return cardEntity.getCardKey();
    }

    @Override
    public Amount toAmount(Double amount) {
        return Amount.inEUR(amount);
    }

    @Override
    public TimelineFRRequest createTimelineRequest(Integer cardIndex) {
        TimelineFRRequest request = new TimelineFRRequest();
        request.setTimeZone(AmericanExpressConstants.RequestValue.TIME_ZONE);
        request.setTimeZoneOffset(AmericanExpressConstants.RequestValue.TIME_ZONE_OFFSET);
        request.setSortedIndex(cardIndex);
        request.setLocalTime(new ThreadSafeDateFormat("MM-dd-YYYY'T'HH:mm:ss").format(new Date()));
        request.setPendingChargeEnabled(true);
        request.setPayWithPointsEnabled(true);
        request.setTimestamp(Long.toString(System.currentTimeMillis()));
        return request;
    }
}
