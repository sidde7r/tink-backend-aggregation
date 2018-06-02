package se.tink.backend.sms.otp.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.junit.Test;
import se.tink.backend.sms.otp.config.SmsOtpRateLimitConfig;
import se.tink.backend.sms.otp.core.SmsOtpEvent;
import se.tink.backend.sms.otp.core.SmsOtpEventType;
import se.tink.backend.sms.otp.core.exceptions.PhoneNumberBlockedException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SmsOtpRateLimiterTest {
    @Test
    public void testNullEventsShouldNotBeBlocked() throws PhoneNumberBlockedException {
        new SmsOtpRateLimiter(new SmsOtpRateLimitConfig()).validate(null);
    }

    @Test
    public void testEmptyEventsShouldNotBeBlocked() throws PhoneNumberBlockedException {
        new SmsOtpRateLimiter(new SmsOtpRateLimitConfig()).validate(ImmutableList.of());
    }

    @Test
    public void testOldVerificationAttemptShouldNotBeBlocked() throws PhoneNumberBlockedException {
        // This date is older than the limit and should not block further.
        DateTime eventDate = DateTime.now().minus(SmsOtpRateLimiter.BAD_VERIFICATION_ATTEMPTS_DURATION).minusMinutes(1);

        SmsOtpEvent event = new SmsOtpEvent("+461111111111", SmsOtpEventType.TOO_MANY_VERIFICATION_ATTEMPTS, eventDate);

        new SmsOtpRateLimiter(new SmsOtpRateLimitConfig()).validate(ImmutableList.of(event));
    }

    @Test(expected = PhoneNumberBlockedException.class)
    public void testRecentVerificationAttemptShouldNotBeBlocked() throws PhoneNumberBlockedException {
        DateTime eventDate = DateTime.now().minus(SmsOtpRateLimiter.BAD_VERIFICATION_ATTEMPTS_DURATION).plusMinutes(1);

        SmsOtpEvent event = new SmsOtpEvent("+461111111111", SmsOtpEventType.TOO_MANY_VERIFICATION_ATTEMPTS, eventDate);

        new SmsOtpRateLimiter(new SmsOtpRateLimitConfig()).validate(ImmutableList.of(event));
    }

    @Test
    public void testBlockingwhenToManySmsSent() throws PhoneNumberBlockedException {
        List<SmsOtpEvent> events = Lists.newArrayList();

        SmsOtpRateLimitConfig config = new SmsOtpRateLimitConfig();
        config.setLimit(5);
        config.setDuration(Hours.hours(24).toPeriod());

        SmsOtpRateLimiter limiter = new SmsOtpRateLimiter(config);

        for (int i = 1; i <= config.getLimit(); i++) {
            events.add(new SmsOtpEvent("+461111111111", SmsOtpEventType.SMS_SENT, DateTime.now().minusHours(i)));

            if (i == config.getLimit()) {
                assertThatThrownBy(() -> limiter.validate(events)).isInstanceOf(PhoneNumberBlockedException.class);
            } else {
                limiter.validate(events);
            }
        }
    }
}
