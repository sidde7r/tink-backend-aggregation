package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData.getTokenResponse;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.data.ChebancaAuthenticatorTestData.getTokenResponseWithoutDataEntity;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.TestAsserts;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class ExchangeAuthorizationCodeTest extends AuthenticatorTestBase {

    @Test
    public void shouldThrowIfUnSuccessfulResponse() {
        // given
        setUpAuthenticatorToCreateToken(getMockedFailedResponse());

        // when
        Throwable thrown =
                catchThrowable(() -> authenticator.exchangeAuthorizationCode("some exchange code"));

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
                getMockedSuccessfulResponse(getTokenResponseWithoutDataEntity()));

        // when
        Throwable thrown =
                catchThrowable(() -> authenticator.exchangeAuthorizationCode("some exchange code"));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(RequiredDataMissingException.class)
                .hasMessage("Data needed to create OAuthToken is missing");
    }

    @Test
    public void shouldExchangeForValidToken() {
        // given
        setUpAuthenticatorToCreateToken(getMockedSuccessfulResponse(getTokenResponse()));

        // when
        OAuth2Token token = authenticator.exchangeAuthorizationCode("some exchange code");

        // then
        TestAsserts.assertValid(token);
    }
}
