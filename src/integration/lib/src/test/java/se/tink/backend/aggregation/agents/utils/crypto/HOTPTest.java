package se.tink.backend.aggregation.agents.utils.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.io.BaseEncoding;
import org.junit.Test;

public class HOTPTest {
    private static final String OTP_SECRET_KEY =
            "OJZWK53WGY2XGOLTNQZDC2LGPF3XO3ZVOQ3WCODPGB2TA6TJN53GENTVPBVDI5ZWOZ3WQ4RYM5TDONTEGVRDA2BWPFUDC4LQ";
    private static final long MOVING_FACTOR = 1052876382L;
    private static final String EXPECTED_VAL = "90329549";

    @Test
    public void generateOTPShouldReturnHOTP() {
        // given
        byte[] otpSecretKey = BaseEncoding.base32().decode(OTP_SECRET_KEY);
        // when
        String hotp = HOTP.generateOTP(otpSecretKey, MOVING_FACTOR, 8, 20);
        // then
        assertThat(hotp).isEqualTo(EXPECTED_VAL);
    }
}
