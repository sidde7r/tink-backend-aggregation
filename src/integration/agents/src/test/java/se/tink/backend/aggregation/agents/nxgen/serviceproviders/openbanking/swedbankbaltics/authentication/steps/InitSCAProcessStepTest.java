package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createSwedbankBalticsAuthenticator;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.rpc.AuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.SwedbankBalticsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.InitSCAProcessStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class InitSCAProcessStepTest {

    private InitSCAProcessStep initSCAProcessStep;

    private SwedbankBalticsAuthenticator authenticator;
    private SwedbankBalticsApiClient apiClient;

    @Before
    public void setUp() {
        authenticator = createSwedbankBalticsAuthenticator();
        apiClient = mock(SwedbankBalticsApiClient.class);

        StepDataStorage stepDataStorage = mock(StepDataStorage.class);
        initSCAProcessStep = new InitSCAProcessStep(authenticator, apiClient, stepDataStorage);
    }

    @Test
    public void shouldExecuteNextStep() throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        final AuthenticationResponse authenticationResponse = createAuthenticationResponse();

        when(apiClient.authenticateDecoupledBaltics(anyString(), anyString()))
                .thenReturn(authenticationResponse);

        // when
        final AuthenticationStepResponse returnedResponse =
                initSCAProcessStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldThrowIncorrectCredentialsException()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest =
                createAuthenticationRequest(
                        SwedbankBalticsHelper.EMPTY_USERNAME, SwedbankBalticsHelper.EMPTY_USERNAME);
        final AuthenticationResponse authenticationResponse = createAuthenticationResponse();

        when(apiClient.authenticateDecoupledBaltics(anyString(), anyString()))
                .thenReturn(authenticationResponse);

        // when
        final Throwable thrown =
                catchThrowable(() -> initSCAProcessStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }
}
