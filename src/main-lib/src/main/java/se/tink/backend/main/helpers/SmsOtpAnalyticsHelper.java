package se.tink.backend.main.helpers;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.core.User;
import se.tink.backend.sms.otp.core.SmsOtpVerificationResult;

public class SmsOtpAnalyticsHelper {
    private static final String SMS_OTP_SENT_EVENT = "user.sms-otp-sent";
    private static final String SMS_OTP_VERIFIED_EVENT = "user.sms-otp-verified";

    private AnalyticsController analyticsController;

    @Inject
    public SmsOtpAnalyticsHelper(AnalyticsController analyticsController) {
        this.analyticsController = analyticsController;
    }

    /**
     * Track the SMS OTP if it belongs to a specific user.
     */
    public void trackSmsSent(Optional<User> user, String phoneNumber, Optional<String> remoteAddress) {
        if (!user.isPresent()) {
            return;
        }

        Map<String, Object> properties = ImmutableMap.of("phone-number", phoneNumber);
        analyticsController.trackUserEvent(user.get(), SMS_OTP_SENT_EVENT, properties, remoteAddress);
    }

    /**
     * Track the SMS OTP if it is belongs to a specific user.
     */
    public void trackSmsVerified(Optional<User> user, SmsOtpVerificationResult result, Optional<String> remoteAddress) {
        if (!user.isPresent()) {
            return;
        }

        Map<String, Object> properties = ImmutableMap.of("verification-result", result.toString());
        analyticsController.trackUserEvent(user.get(), SMS_OTP_VERIFIED_EVENT, properties, remoteAddress);
    }
}
