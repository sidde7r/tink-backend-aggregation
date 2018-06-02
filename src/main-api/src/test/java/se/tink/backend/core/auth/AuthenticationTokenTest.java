package se.tink.backend.core.auth;

import java.util.Map;
import org.junit.Test;
import se.tink.libraries.auth.AuthenticationMethod;
import static org.assertj.core.api.Assertions.assertThat;

public class AuthenticationTokenTest {
    @Test(expected = NullPointerException.class)
    public void testMissingMethod() {
        AuthenticationToken.builder().build();
    }

    @Test(expected = NullPointerException.class)
    public void testMissingStatus() {
        AuthenticationToken.builder()
                .withMethod(AuthenticationMethod.BANKID)
                .build();
    }

    @Test(expected = NullPointerException.class)
    public void testMissingUserIdIfAuthenticated() {
        AuthenticationToken.builder()
                .withMethod(AuthenticationMethod.BANKID)
                .withStatus(AuthenticationStatus.AUTHENTICATED)
                .build();
    }

    @Test
    public void testCorrectAuthenticatedConstruction() {
        AuthenticationToken.builder()
                .withMethod(AuthenticationMethod.BANKID)
                .withStatus(AuthenticationStatus.AUTHENTICATED)
                .withUserId("userId")
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testUserIdProvidedIfStatusIsNoUser() {
        AuthenticationToken.builder()
                .withMethod(AuthenticationMethod.BANKID)
                .withStatus(AuthenticationStatus.NO_USER)
                .withUserId("userId")
                .build();
    }

    @Test
    public void testCorrectConstruction() {
        AuthenticationToken token = AuthenticationToken.builder()
                .withMethod(AuthenticationMethod.BANKID)
                .withStatus(AuthenticationStatus.AUTHENTICATED)
                .withOAuth2ClientId("oAuth2ClientId")
                .withClientKey("clientKey")
                .withToken("token")
                .withUserId("userId")

                // Dynamic payload fields
                .withUsername("username")
                .withHashedPassword("password")
                .withMarket("market")
                .withNationalId("nationalId")

                .build();

        assertThat(token.getMethod()).isEqualTo(AuthenticationMethod.BANKID);
        assertThat(token.getStatus()).isEqualTo(AuthenticationStatus.AUTHENTICATED);
        assertThat(token.getUserId()).isEqualTo("userId");
        assertThat(token.getOAuth2ClientId()).isEqualTo("oAuth2ClientId");
        assertThat(token.getClientKey()).isEqualTo("clientKey");
        assertThat(token.getToken()).isEqualTo("token");
        assertThat(token.getUserId()).isEqualTo("userId");
        assertThat(token.getCreated()).isNotNull();

        Map<AuthenticationPayloadKey, String> payload = token.getPayload();

        assertThat(payload.get(AuthenticationPayloadKey.USERNAME)).isEqualTo("username");
        assertThat(payload.get(AuthenticationPayloadKey.HASHED_PASSWORD)).isEqualTo("password");
        assertThat(payload.get(AuthenticationPayloadKey.MARKET)).isEqualTo("market");
        assertThat(payload.get(AuthenticationPayloadKey.NATIONAL_ID)).isEqualTo("nationalId");
    }
}
