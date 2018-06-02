package se.tink.backend.rpc;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class InitiateBankIdAuthenticationCommandTest {
    @Test
    public void testCorrectConstruction() {
        InitiateBankIdAuthenticationCommand command = InitiateBankIdAuthenticationCommand.builder()
                .withNationalId("nationalId")
                .withClient("client")
                .withDeviceId("deviceId")
                .withMarket("SE")
                .withOauth2ClientId("oauth2ClientId")
                .build();

        assertThat(command.getNationalId().orElse(null)).isEqualTo("nationalId");
        assertThat(command.getClientId().orElse(null)).isEqualTo("client");
        assertThat(command.getDeviceId().orElse(null)).isEqualTo("deviceId");
        assertThat(command.getMarket().orElse(null)).isEqualTo("SE");
        assertThat(command.getOauth2ClientId().orElse(null)).isEqualTo("oauth2ClientId");
    }

    @Test(expected = IllegalStateException.class)
    public void testBothNationalIdAndAuthenticationToken() {
        InitiateBankIdAuthenticationCommand.builder()
                .withNationalId("nationalId")
                .withAuthenticationToken("authenticationToken")
                .build();
    }

    @Test
    public void testNationalIdNorAuthenticationToken() {
        // This is fine since it will trigger BankId with autostart token
        InitiateBankIdAuthenticationCommand.builder().build();
    }

    @Test
    public void testNationalIdShouldBePickedOverAuthenticationToken() {
        InitiateBankIdAuthenticationCommand command = InitiateBankIdAuthenticationCommand.builder()
                .withNationalId("nationalId")
                .withAuthenticationToken("") // To test grpc serialization when we get empty string as default value
                .build();

        assertThat(command.getNationalId().orElse(null)).isEqualTo("nationalId");
        assertThat(command.getAuthenticationToken().isPresent()).isFalse();
    }
}
