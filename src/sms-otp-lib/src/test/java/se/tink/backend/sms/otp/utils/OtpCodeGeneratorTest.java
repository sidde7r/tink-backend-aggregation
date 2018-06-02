package se.tink.backend.sms.otp.otp.utils;

import java.util.Set;
import org.assertj.core.data.Percentage;
import org.assertj.core.util.Sets;
import org.junit.Test;
import se.tink.backend.sms.otp.core.OtpType;
import se.tink.backend.sms.otp.utils.OtpCodeGenerator;
import static org.assertj.core.api.Assertions.assertThat;

public class OtpCodeGeneratorTest {
    @Test(expected = IllegalStateException.class)
    public void shortCodeShouldThroughException() {
        OtpCodeGenerator.generateCode(3);
    }

    @Test(expected = IllegalStateException.class)
    public void longCodeShouldThroughException() {
        OtpCodeGenerator.generateCode(10);
    }

    @Test
    public void verifyOutputLength() {
        for (int i = 4; i <= 8; i++) {

            int count = 0;

            // Run it a couple of times to me on the safe side
            while (count < 1000) {
                String code = OtpCodeGenerator.generateCode(i);
                assertThat(code.length()).isEqualTo(i);
                count++;
            }
        }
    }

    @Test
    public void verifyAlphaOutputLength() {
        for (int i = 4; i <= 8; i++) {
            for (int ii = 100; ii > 0; ii--) {
                String code = OtpCodeGenerator.generateCode(OtpType.ALPHA, i);
                assertThat(code.length()).isEqualTo(i);
            }
        }
    }

    @Test
    public void verifyAlphaOutputEntropy() {
        int numberOfCodes = 2000;
        Set<String> uniqueCodes = Sets.newHashSet();

        // Generate a large amount of codes
        for (int i = numberOfCodes; i > 0; i--) {
            String code = OtpCodeGenerator.generateCode(OtpType.ALPHA, 5);
            uniqueCodes.add(code);
        }

        // The risk of this giving a false negative is really, really small
        assertThat(uniqueCodes.size()).isCloseTo(numberOfCodes, Percentage.withPercentage(5));

        // Check for off-by-one errors by checking first and last character in set
        // Statistically, every character should appear ~476 times
        assertThat(uniqueCodes.stream().anyMatch(code -> code.contains("B"))).isTrue();
        assertThat(uniqueCodes.stream().anyMatch(code -> code.contains("Z"))).isTrue();
    }
}
