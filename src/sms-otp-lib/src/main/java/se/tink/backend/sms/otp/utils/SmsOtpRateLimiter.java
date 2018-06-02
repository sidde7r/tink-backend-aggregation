package se.tink.backend.sms.otp.utils;

import com.google.inject.Inject;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Seconds;
import se.tink.backend.sms.otp.config.SmsOtpRateLimitConfig;
import se.tink.backend.sms.otp.core.SmsOtpEvent;
import se.tink.backend.sms.otp.core.SmsOtpEventType;
import se.tink.backend.sms.otp.core.exceptions.PhoneNumberBlockedException;

public class SmsOtpRateLimiter {
    final static Seconds BAD_VERIFICATION_ATTEMPTS_DURATION = Seconds.seconds(5);
    private final static int BAD_VERIFICATION_ATTEMPTS_LIMIT = 1;
    private SmsOtpRateLimitConfig config;

    @Inject
    public SmsOtpRateLimiter(SmsOtpRateLimitConfig config) {
        this.config = config;
    }

    public void validate(List<SmsOtpEvent> events) throws PhoneNumberBlockedException {
        if (isBlockedDueRecentFailedVerificationAttempt(events)) {
            throw new PhoneNumberBlockedException(
                    "Phone number is blocked because of too many verification attempts on the previous sms.");
        }

        if (isBlockedDueToTooManySmsSent(events)) {
            throw new PhoneNumberBlockedException(
                    "Phone number is blocked because of too many sms sent for the same phone number.");
        }
    }

    private static boolean isBlockedDueRecentFailedVerificationAttempt(List<SmsOtpEvent> events) {
        final DateTime date = new DateTime().minus(BAD_VERIFICATION_ATTEMPTS_DURATION);

        return countEventsOfTypeAfter(events, date, SmsOtpEventType.TOO_MANY_VERIFICATION_ATTEMPTS)
                >= BAD_VERIFICATION_ATTEMPTS_LIMIT;
    }

    private boolean isBlockedDueToTooManySmsSent(List<SmsOtpEvent> events) {
        final DateTime date = new DateTime().minus(config.getDuration());

        return countEventsOfTypeAfter(events, date, SmsOtpEventType.SMS_SENT) >= config.getLimit();
    }

    private static long countEventsOfTypeAfter(List<SmsOtpEvent> events, DateTime date, SmsOtpEventType type) {
        if (events == null) {
            return 0;
        }

        return events.stream().filter(x -> type == x.getType() && date.isBefore(x.getTimestamp().getTime())).count();
    }
}
