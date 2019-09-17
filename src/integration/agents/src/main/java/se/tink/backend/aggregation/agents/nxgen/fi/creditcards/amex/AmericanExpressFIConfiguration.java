package se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.fi.creditcards.amex.fetcher.rpc.TimelineFIRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressFIConfiguration implements AmericanExpressV62Configuration {

    @Override
    public String getLocale() {
        return AmericanExpressFIConstants.BodyValues.LOCALE;
    }

    @Override
    public String getAppId() {
        return AmericanExpressFIConstants.HeaderValues.APP_ID;
    }

    @Override
    public String getUserAgent() {
        return AmericanExpressFIConstants.HeaderValues.USER_AGENT;
    }

    @Override
    public String getBankId(CardEntity cardEntity) {
        return cardEntity.getCardNumberDisplay();
    }

    @Override
    public String getCurrency() {
        return "EUR";
    }

    @Override
    public TimelineFIRequest createTimelineRequest(Integer cardIndex) {
        TimelineFIRequest request = new TimelineFIRequest();
        request.setTimeZone(AmericanExpressV62Constants.RequestValue.TIME_ZONE);
        request.setTimeZoneOffset(AmericanExpressV62Constants.RequestValue.TIME_ZONE_OFFSET);
        request.setSortedIndex(cardIndex);
        request.setLocalTime(new ThreadSafeDateFormat("MM-dd-YYYY'T'HH:mm:ss").format(new Date()));
        request.setPendingChargeEnabled(true);
        request.setPayWithPointsEnabled(true);
        request.setTimestamp(Long.toString(System.currentTimeMillis()));
        return request;
    }
}
