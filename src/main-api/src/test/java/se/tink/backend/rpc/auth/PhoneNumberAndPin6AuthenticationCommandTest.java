package se.tink.backend.rpc.auth;

import org.junit.Test;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import static org.assertj.core.api.Assertions.assertThat;

public class PhoneNumberAndPin6AuthenticationCommandTest {
    @Test
    public void testCorrectConstruction() throws InvalidPhoneNumberException {
        PhoneNumberAndPin6AuthenticationCommand command = PhoneNumberAndPin6AuthenticationCommand.builder()
                .withPhoneNumber("+46701111111")
                .withClientKey("client-key")
                .withMarket("SE")
                .withOauthClientId("oauth-client-id")
                .withUserDeviceId("deviceId")
                .withPin6("121212")
                .withUserAgent("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)")
                .build();

        assertThat(command.getPhoneNumber()).isEqualTo("+46701111111");
        assertThat(command.getClientKey()).isEqualTo("client-key");
        assertThat(command.getMarket()).isEqualTo("SE");
        assertThat(command.getOauthClientId()).isEqualTo("oauth-client-id");
        assertThat(command.getPin6()).isEqualTo("121212");
        assertThat(command.getUserDeviceId()).isEqualTo("deviceId");
        assertThat(command.getUserAgent()).isEqualTo("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)");
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingPhoneNumber() throws InvalidPhoneNumberException {
        PhoneNumberAndPin6AuthenticationCommand.builder()
                .withPhoneNumber(null)
                .withClientKey("client-key")
                .withMarket("SE")
                .withUserDeviceId("deviceId")
                .withOauthClientId("oauth-client-id")
                .withPin6("121212")
                .withUserAgent("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)")
                .build();
    }

    @Test(expected = InvalidPhoneNumberException.class)
    public void testInvalidPhoneNumber() throws InvalidPhoneNumberException {
        PhoneNumberAndPin6AuthenticationCommand.builder()
                .withPhoneNumber("dfdsf")
                .withClientKey("client-key")
                .withMarket("SE")
                .withUserDeviceId("deviceId")
                .withOauthClientId("oauth-client-id")
                .withPin6("121212")
                .withUserAgent("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingPin6() throws InvalidPhoneNumberException {
        PhoneNumberAndPin6AuthenticationCommand.builder()
                .withPhoneNumber("+46709202541")
                .withClientKey("client-key")
                .withMarket("SE")
                .withUserDeviceId("deviceId")
                .withOauthClientId("oauth-client-id")
                .withPin6(null)
                .withUserAgent("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingDeviceId() throws InvalidPhoneNumberException {
        PhoneNumberAndPin6AuthenticationCommand.builder()
                .withPhoneNumber("+46709202541")
                .withClientKey("client-key")
                .withMarket("SE")
                .withOauthClientId("oauth-client-id")
                .withPin6("121212")
                .withUserAgent("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testMissingUserAgent() throws InvalidPhoneNumberException {
        PhoneNumberAndPin6AuthenticationCommand.builder()
                .withPhoneNumber("+46709202541")
                .withClientKey("client-key")
                .withMarket("SE")
                .withOauthClientId("oauth-client-id")
                .withPin6("121212")
                .withUserDeviceId("deviceId")
                .build();
    }
}
