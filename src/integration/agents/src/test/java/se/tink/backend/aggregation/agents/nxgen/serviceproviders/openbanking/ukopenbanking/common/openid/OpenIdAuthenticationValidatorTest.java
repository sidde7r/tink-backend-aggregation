package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.UkOpenBankingTestFixtures.CORRECT_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.UkOpenBankingTestFixtures.INCORRECT_TOKEN_EXPIRED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.UkOpenBankingTestFixtures.INCORRECT_TOKEN_WRONG_TYPE;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Token;

public class OpenIdAuthenticationValidatorTest {

    private OpenIdAuthenticationValidator authenticationValidator;

    @Before
    public void setUp() {
        final OpenIdApiClient apiClientMock = mock(OpenIdApiClient.class);
        authenticationValidator = new OpenIdAuthenticationValidator(apiClientMock);
    }

    @Test
    public void shouldValidateCorrectAccessToken() {
        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                authenticationValidator.validateRefreshableAccessToken(
                                        CORRECT_TOKEN));

        // then
        assertThat(thrown).isNull();
    }

    @Test
    public void shouldValidateAccessTokenWithWrongType() {
        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                authenticationValidator.validateRefreshableAccessToken(
                                        INCORRECT_TOKEN_WRONG_TYPE));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasNoCause()
                .hasMessage("[OpenIdAuthenticationValidator] Unknown token type 'Hmac'.");
    }

    @Test
    public void shouldValidateExpiredAccessToken() {
        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                authenticationValidator.validateRefreshableAccessToken(
                                        INCORRECT_TOKEN_EXPIRED));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasNoCause()
                .hasMessage("[OpenIdAuthenticationValidator] Invalid access token.");
    }

    @Test
    public void shouldValidateCorrectClientToken() {
        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                authenticationValidator.validateToken(
                                        CORRECT_TOKEN, Token.CLIENT_ACCESS_TOKEN_MSG));

        // then
        assertThat(thrown).isNull();
    }

    @Test
    public void shouldValidateIncorrectClientToken() {
        // when
        final Throwable thrown =
                catchThrowable(
                        () ->
                                authenticationValidator.validateToken(
                                        INCORRECT_TOKEN_EXPIRED, Token.CLIENT_ACCESS_TOKEN_MSG));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(SessionException.class)
                .hasNoCause()
                .hasMessage("[OpenIdAuthenticationValidator] Invalid client access token.");
    }
}
