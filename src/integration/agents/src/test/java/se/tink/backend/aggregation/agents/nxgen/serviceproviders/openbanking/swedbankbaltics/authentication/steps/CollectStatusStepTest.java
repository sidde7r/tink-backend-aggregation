package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createSwedbankBalticsAuthenticator;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.CollectStatusStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class CollectStatusStepTest {

    private SwedbankBalticsAuthenticator authenticator;
    private SwedbankBalticsApiClient apiClient;
    private StepDataStorage stepDataStorage;

    private CollectStatusStep collectStatusStep;
    private AuthenticationStatusResponse authenticationStatusResponse;

    @Before
    public void setUp() {
        authenticator = createSwedbankBalticsAuthenticator();
        stepDataStorage = mock(StepDataStorage.class);
        apiClient = mock(SwedbankBalticsApiClient.class);

        collectStatusStep = new CollectStatusStep(authenticator, apiClient, stepDataStorage);

        authenticationStatusResponse = mock(AuthenticationStatusResponse.class);
    }

    @Test
    public void shouldExecuteNextStep() throws AuthenticationException, AuthorizationException {

        // give
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(stepDataStorage.getAuthUrl()).thenReturn(SwedbankBalticsHelper.DUMMY_URI);
        when(apiClient.collectBalticAuthStatus(anyString(), anyString()))
                .thenReturn(authenticationStatusResponse);

        when(authenticationStatusResponse.loginCanceled()).thenReturn(false);
        when(authenticationStatusResponse.getScaStatus()).thenReturn(AuthStatus.FINALIZED);

        // when
        final AuthenticationStepResponse returnedResponse =
                collectStatusStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldThrowAuthenticationError()
            throws AuthenticationException, AuthorizationException {
        // give
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(stepDataStorage.getAuthUrl()).thenReturn(SwedbankBalticsHelper.DUMMY_URI);

        HttpResponseException e = mock(HttpResponseException.class);

        HttpResponse httpResponse = mock(HttpResponse.class);
        when(e.getResponse()).thenReturn(httpResponse);
        when(httpResponse.getStatus()).thenReturn(500);
        when(httpResponse.getBody(String.class)).thenReturn(SwedbankBalticsHelper.DUMMY_STRING);

        doThrow(e).when(apiClient).collectBalticAuthStatus(anyString(), anyString());

        // when
        final Throwable thrown =
                catchThrowable(() -> collectStatusStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.AUTHENTICATION_ERROR");
    }

    @Test
    public void shouldThrowAuthenticationErrorWhenStatusFailed()
            throws AuthenticationException, AuthorizationException {

        // give
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(stepDataStorage.getAuthUrl()).thenReturn(SwedbankBalticsHelper.DUMMY_URI);
        when(apiClient.collectBalticAuthStatus(anyString(), anyString()))
                .thenReturn(authenticationStatusResponse);

        when(authenticationStatusResponse.loginCanceled()).thenReturn(false);
        when(authenticationStatusResponse.getScaStatus()).thenReturn(AuthStatus.FAILED);

        // when
        final Throwable thrown =
                catchThrowable(() -> collectStatusStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.AUTHENTICATION_ERROR");
    }

    @Test
    public void shouldThrowAuthenticationErrorWhenUnknownStatus()
            throws AuthenticationException, AuthorizationException {

        // give
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(stepDataStorage.getAuthUrl()).thenReturn(SwedbankBalticsHelper.DUMMY_URI);
        when(apiClient.collectBalticAuthStatus(anyString(), anyString()))
                .thenReturn(authenticationStatusResponse);

        when(authenticationStatusResponse.loginCanceled()).thenReturn(false);
        when(authenticationStatusResponse.getScaStatus())
                .thenReturn(SwedbankBalticsHelper.UNKNOWN_STATUS);

        // when
        final Throwable thrown =
                catchThrowable(() -> collectStatusStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.AUTHENTICATION_ERROR");
    }

    @Test
    public void shouldThrowTimeoutException()
            throws AuthenticationException, AuthorizationException {

        // give
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(stepDataStorage.getAuthUrl()).thenReturn(SwedbankBalticsHelper.DUMMY_URI);
        when(apiClient.collectBalticAuthStatus(anyString(), anyString()))
                .thenReturn(authenticationStatusResponse);

        when(authenticationStatusResponse.loginCanceled()).thenReturn(false);
        when(authenticationStatusResponse.getScaStatus()).thenReturn(AuthStatus.STARTED);

        // when
        final Throwable thrown =
                catchThrowable(() -> collectStatusStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.TIMED_OUT");
    }

    @Test
    public void shouldThrowCancelledException()
            throws AuthenticationException, AuthorizationException {

        // give
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(stepDataStorage.getAuthUrl()).thenReturn(SwedbankBalticsHelper.DUMMY_URI);
        when(apiClient.collectBalticAuthStatus(anyString(), anyString()))
                .thenReturn(authenticationStatusResponse);

        when(authenticationStatusResponse.loginCanceled()).thenReturn(true);

        // when
        final Throwable thrown =
                catchThrowable(() -> collectStatusStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.CANCELLED");
    }
}
