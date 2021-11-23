package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabAuthorizationCredentials;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class TokenRequestFactoryTest {

    private final URL redirectUri =
            URL.of("https://api.tink.com.fake/api/v1/credentials/third-party/callback");

    private final KnabAuthorizationCredentials authorizationCredentials =
            new KnabAuthorizationCredentials("my-client-id", "my-client-secret");

    private final TokenRequestFactory tokenRequestFactory =
            new TokenRequestFactory(authorizationCredentials, redirectUri.toString());

    @Test
    public void shouldCreateSerializedApplicationAccessTokenRequestBody() {
        // when
        String serializedTokenRequestBody = tokenRequestFactory.applicationAccessTokenRequest();

        // then
        assertThat(serializedTokenRequestBody)
                .isEqualTo("grant_type=client_credentials&scope=psd2");
    }

    @Test
    @SneakyThrows
    public void shouldCreateSerializedAccessTokenRequestBody() {
        // given
        String code = "ACCESS000000";
        String state = "mfa-state-3";

        // when
        String serializedTokenRequestBody = tokenRequestFactory.accessTokenRequest(code, state);

        // then
        assertThat(serializedTokenRequestBody)
                .isEqualTo(
                        String.format(
                                "grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&state=%s&redirect_uri=%s",
                                authorizationCredentials.getClientId(),
                                authorizationCredentials.getClientSecret(),
                                code,
                                state,
                                URLEncoder.encode(
                                        redirectUri.toString(),
                                        StandardCharsets.UTF_8.toString())));
    }

    @Test
    public void shouldCreateSerializedRefreshTokenRequestBody() {
        // given
        String refreshToken = "REFRESH000000";

        // when
        String serializedTokenRequestBody = tokenRequestFactory.refreshTokenRequest(refreshToken);

        // then
        assertThat(serializedTokenRequestBody)
                .isEqualTo(
                        String.format(
                                "grant_type=refresh_token&client_id=%s&client_secret=%s&refresh_token=%s",
                                authorizationCredentials.getClientId(),
                                authorizationCredentials.getClientSecret(),
                                refreshToken));
    }
}
