package se.tink.backend.sms.otp.otp.core;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.junit.Test;
import se.tink.backend.sms.otp.core.OtpType;
import se.tink.backend.sms.otp.core.SmsOtp;
import se.tink.backend.sms.otp.core.SmsOtpConsumeResult;
import se.tink.backend.sms.otp.core.SmsOtpStatus;
import se.tink.backend.sms.otp.core.SmsOtpVerificationResult;
import static org.assertj.core.api.Assertions.assertThat;

public class SmsOtpTest {
    @Test
    public void testBuilder() {
        Seconds ttl = Seconds.seconds(60);

        SmsOtp otp = SmsOtp.builder()
                .withCode("1234")
                .withPhoneNumber("111222")
                .withTimeToLive(ttl)
                .withType(OtpType.NUMERIC)
                .build();

        assertThat(otp.getCode()).isEqualTo("1234");
        assertThat(otp.getId()).isNotNull();
        assertThat(otp.getVerifiedAt()).isNull();
        assertThat(otp.getPhoneNumber()).isEqualTo("111222");
        assertThat(otp.getStatus()).isEqualTo(SmsOtpStatus.NOT_SENT);
        assertThat(otp.getExpireAt()).isEqualTo(new DateTime(otp.getCreatedAt()).plus(ttl).toDate());
    }

    @Test
    public void testVerifyWithWrongStatus() {
        SmsOtp otp = SmsOtp.builder()
                .withCode("1234")
                .withPhoneNumber("111222")
                .withTimeToLive(Seconds.seconds(60))
                .withType(OtpType.NUMERIC)
                .build();

        assertThat(otp.verify("1234", 10)).isEqualTo(SmsOtpVerificationResult.INVALID_OTP_STATUS);
    }

    @Test
    public void testVerifyThatHaveExpired() throws InterruptedException {
        SmsOtp otp = SmsOtp.builder()
                .withCode("1234")
                .withPhoneNumber("111222")
                .withTimeToLive(Seconds.ZERO)
                .withType(OtpType.NUMERIC)
                .build();

        Thread.sleep(2);

        otp.setStatus(SmsOtpStatus.SENT_SUCCESS);

        assertThat(otp.verify("1234", 10)).isEqualTo(SmsOtpVerificationResult.OTP_EXPIRED);
    }

    @Test
    public void testVerifyWithWrongCode() throws InterruptedException {
        SmsOtp otp = SmsOtp.builder()
                .withCode("1234")
                .withPhoneNumber("111222")
                .withTimeToLive(Seconds.seconds(60))
                .withType(OtpType.NUMERIC)
                .build();

        otp.setStatus(SmsOtpStatus.SENT_SUCCESS);

        assertThat(otp.verify("1111", 10)).isEqualTo(SmsOtpVerificationResult.INCORRECT_CODE);
    }

    @Test
    public void testVerifyExceededVerificationAttempts() {
        SmsOtp otp = SmsOtp.builder()
                .withCode("1234")
                .withPhoneNumber("111222")
                .withTimeToLive(Seconds.seconds(60))
                .withType(OtpType.NUMERIC)
                .build();

        otp.setStatus(SmsOtpStatus.SENT_SUCCESS);

        assertThat(otp.getVerificationAttempts()).isEqualTo(0);

        assertThat(otp.verify("1111", 1)).isEqualTo(SmsOtpVerificationResult.INCORRECT_CODE);
        assertThat(otp.getVerificationAttempts()).isEqualTo(1);

        assertThat(otp.verify("1111", 1)).isEqualTo(SmsOtpVerificationResult.TOO_MANY_VERIFICATION_ATTEMPTS);
        assertThat(otp.getVerificationAttempts()).isEqualTo(2);
    }

    @Test
    public void testVerifyCorrectCode() {
        SmsOtp otp = SmsOtp.builder()
                .withCode("1234")
                .withPhoneNumber("111222")
                .withTimeToLive(Seconds.seconds(60))
                .withType(OtpType.NUMERIC)
                .build();

        otp.setStatus(SmsOtpStatus.SENT_SUCCESS);

        assertThat(otp.verify("1234", 10)).isEqualTo(SmsOtpVerificationResult.CORRECT_CODE);
        assertThat(otp.getVerifiedAt()).isNotNull();
        assertThat(otp.getStatus()).isEqualTo(SmsOtpStatus.VERIFIED);
    }

    @Test
    public void testAlphaOtpVerification() {
        SmsOtp otp = SmsOtp.builder()
                .withCode("BNKTNK")
                .withPhoneNumber("111222")
                .withTimeToLive(Minutes.minutes(5))
                .withType(OtpType.ALPHA)
                .build();

        otp.setStatus(SmsOtpStatus.SENT_SUCCESS);

        assertThat(otp.verify("BNKTNK", 10)).isEqualTo(SmsOtpVerificationResult.CORRECT_CODE);
    }

    @Test
    public void testAlphaOtpVerificationFailed() {
        SmsOtp otp = SmsOtp.builder()
                .withCode("BNKTNK")
                .withPhoneNumber("111222")
                .withTimeToLive(Minutes.minutes(5))
                .withType(OtpType.ALPHA)
                .build();

        otp.setStatus(SmsOtpStatus.SENT_SUCCESS);

        assertThat(otp.verify("NOTTNK", 10)).isEqualTo(SmsOtpVerificationResult.INCORRECT_CODE);
    }

    @Test
    public void testConsumeWhenVerified() {
        SmsOtp otp = createVerifiedOtp();

        assertThat(otp.consume()).isEqualTo(SmsOtpConsumeResult.CONSUMED);
        assertThat(otp.getStatus()).isEqualTo(SmsOtpStatus.CONSUMED);
    }

    @Test
    public void testConsumeWhenAlreadyConsumed() {
        SmsOtp otp = createVerifiedOtp();

        assertThat(otp.consume()).isEqualTo(SmsOtpConsumeResult.CONSUMED);
        assertThat(otp.getStatus()).isEqualTo(SmsOtpStatus.CONSUMED);

        // Should not be possible to consume again
        assertThat(otp.consume()).isEqualTo(SmsOtpConsumeResult.ALREADY_CONSUMED);
        assertThat(otp.getStatus()).isEqualTo(SmsOtpStatus.CONSUMED); // Same status as before
    }

    private SmsOtp createVerifiedOtp() {
        SmsOtp otp = SmsOtp.builder()
                .withCode("1234")
                .withPhoneNumber("111222")
                .withTimeToLive(Seconds.seconds(60))
                .withType(OtpType.NUMERIC)
                .build();

        otp.setStatus(SmsOtpStatus.SENT_SUCCESS);

        assertThat(otp.verify("1234", 10)).isEqualTo(SmsOtpVerificationResult.CORRECT_CODE);

        return otp;
    }
}
