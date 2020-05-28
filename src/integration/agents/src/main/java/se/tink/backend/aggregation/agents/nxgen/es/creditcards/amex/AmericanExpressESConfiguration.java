package se.tink.backend.aggregation.agents.nxgen.es.creditcards.amex;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.creditcards.amex.fetcher.rpc.TimelineESRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressESConfiguration implements AmericanExpressV62Configuration {

    @Override
    public String getAppId() {
        return AmericanExpressESConstants.HeaderValues.APP_ID;
    }

    @Override
    public String getInitVersion() {
        return AmericanExpressESConstants.INIT_VERSION;
    }

    @Override
    public String getUserAgent() {
        return AmericanExpressESConstants.HeaderValues.USER_AGENT;
    }

    @Override
    public String getLocale() {
        return AmericanExpressESConstants.HeaderValues.LOCALE;
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
    public TimelineRequest createTimelineRequest(Integer cardIndex) {
        TimelineESRequest request = new TimelineESRequest();
        request.setTimeZone(AmericanExpressV62Constants.RequestValue.TIME_ZONE);
        request.setTimeZoneOffset(AmericanExpressV62Constants.RequestValue.TIME_ZONE_OFFSET);
        request.setSortedIndex(cardIndex);
        request.setLocalTime(new ThreadSafeDateFormat("MM-dd-YYYY'T'HH:mm:ss").format(new Date()));
        request.setPendingChargeEnabled(true);
        request.setCmlEnabled(true);
        request.setTimestamp(Long.toString(System.currentTimeMillis()));
        return request;
    }

    @Override
    public String getAppVersion() {
        return AmericanExpressESConstants.HeaderValues.APP_VERSION;
    }
}
