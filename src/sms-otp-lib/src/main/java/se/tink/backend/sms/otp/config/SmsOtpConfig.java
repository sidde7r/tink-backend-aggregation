package se.tink.backend.sms.otp.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.Minutes;
import org.joda.time.Period;
import se.tink.backend.sms.otp.core.OtpType;

public class SmsOtpConfig {
    @JsonProperty
    private boolean enabled = false;

    @JsonProperty
    private boolean dummyMode = false;

    @JsonProperty
    private int otpLength = 5;

    @JsonProperty
    private int maxVerificationAttempts = 3;

    @JsonProperty
    private Period timeToLive = Minutes.minutes(2).toPeriod();

    @JsonProperty
    private String sender = null;

    @JsonProperty
    private OtpType type = OtpType.NUMERIC;

    @JsonProperty
    private SmsOtpRateLimitConfig rateLimit = new SmsOtpRateLimitConfig();

    public int getOtpLength() {
        return otpLength;
    }

    public int getMaxVerificationAttempts() {
        return maxVerificationAttempts;
    }

    public String getSender() {
        return sender;
    }

    public Period getTimeToLive() {
        return timeToLive;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDummyMode() {
        return dummyMode;
    }

    public OtpType getType() {
        return type;
    }

    public SmsOtpRateLimitConfig getRateLimit() {
        return rateLimit;
    }
}
