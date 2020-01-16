package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.detail;

import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData.ACCESS_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData.ACCESS_TOKEN_EXPIRES_IN;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData.REFRESH_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData.REFRESH_TOKEN_EXPIRES_IN;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData.TOKEN_TYPE;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.TestAsserts;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class TokenResponseConverterTest {

    @Test
    public void shouldGetValidToken() {
        // given
        TokenResponse tokenResponse = ChebancaAuthenticatorTestData.getTokenResponse();

        // when
        OAuth2Token token = TokenResponseConverter.toOAuthToken(tokenResponse);

        // then
        TestAsserts.assertEqual(getExpectedToken(), token);
    }

    @Test
    public void shouldThrowIfRequiredDataIsMissing() {

        // given
        TokenResponse tokenResponse =
                ChebancaAuthenticatorTestData.getTokenResponseWithoutDataEntity();

        // when
        Throwable thrown = catchThrowable(() -> TokenResponseConverter.toOAuthToken(tokenResponse));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(RequiredDataMissingException.class)
                .hasMessage("Data needed to create OAuthToken is missing");
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
