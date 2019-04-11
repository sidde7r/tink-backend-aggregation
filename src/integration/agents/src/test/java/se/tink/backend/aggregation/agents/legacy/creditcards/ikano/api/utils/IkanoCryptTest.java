package se.tink.backend.aggregation.agents.creditcards.ikano.api.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.security.NoSuchAlgorithmException;
import org.assertj.core.util.Strings;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;

public class IkanoCryptTest {

    @Test
    public void generateDeviceIdTest() {
        String testDeviceId = "6DE9ABED-1A8A-42CA-8435-3CE2828C6594....";
        String deviceId = IkanoCrypt.generateDeviceID("198903083216");

        assertThat(deviceId.length()).isEqualTo(testDeviceId.length());
        assertThat(deviceId).endsWith(".");
    }

    @Test
    public void generateDeviceAuthTest() throws NoSuchAlgorithmException {
        String deviceId = "6DE9ABED-1A8A-42CA-8435-3CE2828C6594....";
        String deviceAuth = IkanoCrypt.generateDeviceAuth(deviceId);

        assertThat(deviceAuth).isEqualTo("c573cbe5ed0e4c8ce2d61804886ab478");
    }

    @Test
    public void findOrGenerateExistingDeviceId() {
        Credentials credentials = new Credentials();
        credentials.setUsername("test@test.com");
        credentials.setSensitivePayload("deviceId", "6DE9ABED-1A8A-42CA-8435-3CE2828C6594....");

        String deviceId = IkanoCrypt.findOrGenerateDeviceIdFor(credentials);

        assertThat(deviceId).isEqualTo("6DE9ABED-1A8A-42CA-8435-3CE2828C6594....");
    }

    @Test
    public void findOrGenerateMissingDeviceId() {
        Credentials credentials = new Credentials();
        credentials.setUsername("test@test.com");

        String deviceId = IkanoCrypt.findOrGenerateDeviceIdFor(credentials);

        Assert.assertFalse(Strings.isNullOrEmpty(deviceId));
        assertThat(deviceId).isEqualTo(credentials.getSensitivePayload("deviceId"));
    }
}
