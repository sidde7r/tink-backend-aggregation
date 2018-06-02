package se.tink.backend.rpc.auth;

import java.util.UUID;
import org.junit.Test;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;

public class SmsOtpAndPin6AuthenticationCommandTest {
    private String remoteAddress = "127.0.0.1";
    private String deviceId = UUID.randomUUID().toString();

    @Test
    public void testCorrectConstruction() throws InvalidPin6Exception {
        new SmsOtpAndPin6AuthenticationCommand("otp", "121212", "clientKey", "oauth", "NL", deviceId, remoteAddress);
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingOtpVerificationToken() throws InvalidPin6Exception {
        new SmsOtpAndPin6AuthenticationCommand(null, "121212", "clientKey", "oauthClientId", "NL", deviceId,
                remoteAddress);
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingPin6Code() throws InvalidPin6Exception {
        new SmsOtpAndPin6AuthenticationCommand("otp", null, "clientKey", "oauth", "NL", deviceId, remoteAddress);
    }
}
