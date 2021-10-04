package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationRequest;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.ExchangeCodeForTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ExchangeCodeForTokenStepTest {

    private ExchangeCodeForTokenStep exchangeCodeForTokenStep;
    private SwedbankBalticsApiClient apiClient;

    @Before
    public void setUp() {
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        StepDataStorage stepDataStorage = mock(StepDataStorage.class);

        when(stepDataStorage.getAuthCode()).thenReturn(SwedbankBalticsHelper.DUMMY_AUTH_CODE);

        apiClient = mock(SwedbankBalticsApiClient.class);
        exchangeCodeForTokenStep =
                new ExchangeCodeForTokenStep(apiClient, persistentStorage, stepDataStorage);
    }

    @Test
    public void shouldExecuteNextStep() throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        OAuth2Token accessToken = mock(OAuth2Token.class);
        when(apiClient.exchangeCodeForToken(anyString())).thenReturn(accessToken);

        // when
        final AuthenticationStepResponse returnedResponse =
                exchangeCodeForTokenStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldThrowAuthenticationError()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        doThrow(ThirdPartyAppError.AUTHENTICATION_ERROR.exception())
                .when(apiClient)
                .exchangeCodeForToken(anyString());

        // when
        final Throwable thrown =
                catchThrowable(() -> exchangeCodeForTokenStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.AUTHENTICATION_ERROR");
    }
}
