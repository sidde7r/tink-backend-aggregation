package se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62;

import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TimelineRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.creditcards.amex.v62.fetcher.rpc.TimelineUKRequest;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class AmericanExpressV62UKConfiguration implements AmericanExpressV62Configuration {

    @Override
    public String getLocale() {
        return AmericanExpressV62UKConstants.HeaderValues.LOCALE;
    }

    @Override
    public String getBankId(CardEntity cardEntity) {
        return cardEntity.getCardNumberDisplay();
    }

    @Override
    public String getCurrency() {
        return "GBP";
    }

    @Override
    public TimelineRequest createTimelineRequest(Integer cardIndex) {
        TimelineUKRequest request = new TimelineUKRequest();
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
    public String getAppId() {
        return AmericanExpressV62UKConstants.HeaderValues.APP_ID;
    }

    @Override
    public String getInitVersion() {
        return AmericanExpressV62UKConstants.INIT_VERSION;
    }

    @Override
    public String getUserAgent() {
        return AmericanExpressV62UKConstants.HeaderValues.USER_AGENT;
    }

    public String getGitSha() {
        return AmericanExpressV62UKConstants.HeaderValues.GIT_SHA;
    }

    @Override
    public String getAppVersion() {
        return AmericanExpressV62UKConstants.HeaderValues.APP_VERSION;
    }
}
