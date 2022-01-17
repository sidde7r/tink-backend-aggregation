package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.detail;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.data.NorwegianAuthenticatorTestData.ACCESS_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.data.NorwegianAuthenticatorTestData.ACCESS_TOKEN_EXPIRES_IN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.data.NorwegianAuthenticatorTestData.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.data.NorwegianAuthenticatorTestData.REFRESH_TOKEN_EXPIRES_IN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.data.NorwegianAuthenticatorTestData.TOKEN_TYPE;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.data.NorwegianAuthenticatorTestData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.norwegian.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class TokenResponseConverterTest {

    @Test
    public void shouldGetValidToken() {
        // given
        TokenResponse tokenResponse = NorwegianAuthenticatorTestData.getTokenResponse();

        // when
        OAuth2Token token = tokenResponse.toOauthToken();

        // then
        OAuth2Token expected = getExpectedToken();
        assertEquals(expected.getAccessToken(), token.getAccessToken());
        assertEquals(expected.getOptionalRefreshToken(), token.getOptionalRefreshToken());
        assertEquals(expected.getTokenType(), token.getTokenType());
        assertEquals(expected.getAccessExpireEpoch(), token.getAccessExpireEpoch());
    }

    @Test
    public void shouldThrowIfRequiredDataIsMissing() {

        // given
        TokenResponse tokenResponse = NorwegianAuthenticatorTestData.getInvalidTokenResponse();

        // when
        Throwable thrown = catchThrowable(tokenResponse::toOauthToken);

        // then
        Assertions.assertThat(thrown).isInstanceOf(RequiredDataMissingException.class);
    }

    private OAuth2Token getExpectedToken() {
        return OAuth2Token.create(
                TOKEN_TYPE,
                ACCESS_TOKEN,
                REFRESH_TOKEN,
                ACCESS_TOKEN_EXPIRES_IN,
                REFRESH_TOKEN_EXPIRES_IN);
    }
}
