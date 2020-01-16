package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.TestAsserts;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class RefreshAccessTokenTest extends AuthenticatorTestBase {

    @Test
    public void shouldThrowIfUnSuccessfulResponse() {
        // given
        setUpAuthenticatorToCreateToken(getMockedFailedResponse());

        // when
        Throwable thrown =
                catchThrowable(() -> authenticator.refreshAccessToken("some refresh token"));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage(
                        "Could not get OAuth token. Error response code: " + ERROR_RESPONSE_CODE);
    }

    @Test
    public void shouldThrowIfTokenResponseLacksData() {
        // given
        setUpAuthenticatorToCreateToken(
                getMockedSuccessfulResponse(
                        ChebancaAuthenticatorTestData.getTokenResponseWithoutDataEntity()));

        // when
        Throwable thrown =
                catchThrowable(() -> authenticator.refreshAccessToken("some refresh token"));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(RequiredDataMissingException.class)
                .hasMessage("Data needed to create OAuthToken is missing");
    }

    @Test
    public void shouldGetValidRefreshToken() throws SessionException {
        // given
        setUpAuthenticatorToCreateToken(
                getMockedSuccessfulResponse(ChebancaAuthenticatorTestData.getTokenResponse()));

        // when
        OAuth2Token token = authenticator.refreshAccessToken("some refresh token");

        // then
        TestAsserts.assertValid(token);
    }
}
