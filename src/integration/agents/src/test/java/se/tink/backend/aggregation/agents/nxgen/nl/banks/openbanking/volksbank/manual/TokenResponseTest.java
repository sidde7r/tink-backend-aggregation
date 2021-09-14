package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.manual;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TokenResponseTest {

    @Test
    public void shouldReturnOAuth2Token() {
        // given
        TokenResponse tokenResponse = getTokenResponse();

        // then
        assertEquals(getOAuth2Token(), tokenResponse.toOauthToken());
    }

    @Test
    public void shouldNotReturnOAuth2Token() {
        // given
        TokenResponse tokenResponse = getEmptyTokenResponse();

        // then
        assertThatThrownBy(tokenResponse::toOauthToken).isInstanceOf(NumberFormatException.class);
    }

    private OAuth2Token getOAuth2Token() {
        return OAuth2Token.create("bearer", "token123", "refresh123", 600);
    }

    private TokenResponse getTokenResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + " \"access_token\":\"token123\",\n"
                        + "  \"token_type\":\"bearer\",\n"
                        + "  \"expires_in\":600,\n"
                        + "  \"refresh_token\":\"refresh123\",\n"
                        + "  \"scope\":\"AIS\"\n"
                        + "}",
                TokenResponse.class);
    }

    private TokenResponse getEmptyTokenResponse() {
        return SerializationUtils.deserializeFromString("{}", TokenResponse.class);
    }
}
