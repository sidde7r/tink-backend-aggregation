package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.ICSOAuthTokenFactory;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class ICSOAuthTokenFactoryTest {

    private final String clientId = "00000000000000000000000000000001";

    private final String clientSecret = "00000000000000000000000000000002";

    private final URL redirectUri =
            URL.of("https://api.tink.com.fake/api/v1/credentials/third-party/callback");

    private final ICSOAuthTokenFactory tokenFactory =
            new ICSOAuthTokenFactory(clientId, clientSecret, redirectUri.toString());

    @Test
    public void shouldCreateClientCredentialsToken() {
        // when
        String serializedToken = tokenFactory.clientCredentialsToken();

        // then
        assertThat(serializedToken)
                .isEqualTo(
                        String.format(
                                "client_id=%s&client_secret=%s&scope=accounts&grant_type=client_credentials",
                                clientId, clientSecret));
    }

    @Test
    @SneakyThrows
    public void shouldCreateConsentAuthorizationToken() {
        // given
        String authenticationCode = "0000000000000";

        // when
        String serializedToken = tokenFactory.consentAuthorizationToken(authenticationCode);

        // then
        assertThat(serializedToken)
                .isEqualTo(
                        String.format(
                                "client_id=%s&client_secret=%s&scope=accounts&grant_type=authorization_code&code=%s&redirect_uri=%s",
                                clientId,
                                clientSecret,
                                authenticationCode,
                                URLEncoder.encode(
                                        redirectUri.toString(),
                                        StandardCharsets.UTF_8.toString())));
    }

    @Test
    public void shouldCreateRefreshToken() {
        // given
        String refreshToken =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsiYWNjb3VudHMiXSwidHBwSW5mb3JtYXRpb24iOnsidHBwTGVnYWxFbnRpdHlOYW1lIjoiVGluayBBQiBGYWtlIiwidHBwUmVnaXN0ZXJlZElkIjoiVGluayBGYWtlIiwidHBwUm9sZXMiOlsiQUlTUCJdfSwidXNlcl9uYW1lIjoiTW9udHkgUHl0aG9uIiwiYXRpIjoiMDAwMDAwMDAtMDAwMC00MDAwLTAwMDAtMDAwMDAwMDAwMDAwIiwiZXhwIjo3MDI4NjQwMDAsImF1dGhvcml0aWVzIjpbIkNVU1RPTUVSX1JPTEUiXSwianRpIjoiMDAwMDAwMDAtMDAwMC00MDAwLTAwMDAtMDAwMDAwMDAwMDAwIiwiY2xpZW50X2lkIjoiMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDEifQ.uguv_ta1eAmvJe2Od4XsT_W6-QrP7cDhOZTypOzvjFk";

        // when
        String serializedToken = tokenFactory.refreshToken(refreshToken);

        // then
        assertThat(serializedToken)
                .isEqualTo(
                        String.format(
                                "client_id=%s&client_secret=%s&grant_type=refresh_token&refresh_token=%s",
                                clientId, clientSecret, refreshToken));
    }
}
