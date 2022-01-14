package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationRequest;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.RefreshAccessTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class RefreshAccessTokenStepTest {

    private RefreshAccessTokenStep refreshAccessTokenStep;
    private SwedbankBalticsApiClient apiClient;
    private PersistentStorage persistentStorage;
    private OAuth2Token token;
    private String refreshToken;

    @Before
    public void setUp() {
        token = mock(OAuth2Token.class);
        apiClient = mock(SwedbankBalticsApiClient.class);
        persistentStorage = mock(PersistentStorage.class);
        refreshToken = SwedbankBalticsHelper.DUMMY_TOKEN;
        refreshAccessTokenStep = new RefreshAccessTokenStep(apiClient, persistentStorage);
    }

    @Test
    public void shouldExecuteNextStepIfTokenIsNotPresent()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.empty());

        // when
        final AuthenticationStepResponse returnedResponse =
                refreshAccessTokenStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldThrowIllegalStateExceptionIfRefreshTokenIsNotPresent()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(token));
        when(token.canRefresh()).thenReturn(true);
        when(token.getOptionalRefreshToken()).thenReturn(Optional.empty());

        // when
        final Throwable thrown =
                catchThrowable(() -> refreshAccessTokenStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Missing refresh token");
    }

    @Test
    public void shouldGetAllConsentWhenRefreshTokenIsPresentedAndValid()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(token));
        when(token.canRefresh()).thenReturn(true);
        when(token.getOptionalRefreshToken()).thenReturn(Optional.of(refreshToken));

        OAuth2Token newToken = mock(OAuth2Token.class);
        when(apiClient.refreshToken(refreshToken)).thenReturn(newToken);

        when(persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, newToken))
                .thenReturn(SwedbankBalticsHelper.DUMMY_TOKEN);

        // when
        final AuthenticationStepResponse returnedResponse =
                refreshAccessTokenStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isTrue();
        assertThat(returnedResponse.getNextStepId().get())
                .isEqualTo(Steps.GET_CONSENT_FOR_ALL_ACCOUNTS_STEP);
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldExcecuteNextStepIfRefreshWasUnsuccessful()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(token));
        when(token.canRefresh()).thenReturn(true);
        when(token.getOptionalRefreshToken()).thenReturn(Optional.empty());

        // when
        final Throwable thrown =
                catchThrowable(() -> refreshAccessTokenStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("Missing refresh token");
    }
}
