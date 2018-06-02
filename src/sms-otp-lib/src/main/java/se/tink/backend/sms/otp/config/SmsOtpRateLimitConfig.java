package se.tink.backend.sms.otp.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Hours;
import org.joda.time.Period;

/**
 * Default config will allow 10 sms for each phone number per 24h.
 */
public class SmsOtpRateLimitConfig {
    @JsonProperty
    private Period duration = Hours.hours(24).toPeriod();

    @JsonProperty
    private int limit = 10;

    public Period getDuration() {
        return duration;
    }

    public int getLimit() {
        return limit;
    }

    public void setDuration(Period duration) {
        this.duration = duration;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
