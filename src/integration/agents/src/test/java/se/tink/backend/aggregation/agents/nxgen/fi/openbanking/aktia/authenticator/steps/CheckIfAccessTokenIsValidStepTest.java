package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.AktiaTestFixtures.createAuthenticationRequest;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaAccessTokenRetriever;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class CheckIfAccessTokenIsValidStepTest {

    private CheckIfAccessTokenIsValidStep checkIfAccessTokenIsValidStep;

    private AktiaAccessTokenRetriever accessTokenRetrieverMock;

    @Before
    public void setUp() {
        accessTokenRetrieverMock = mock(AktiaAccessTokenRetriever.class);

        checkIfAccessTokenIsValidStep = new CheckIfAccessTokenIsValidStep(accessTokenRetrieverMock);
    }

    @Test
    public void shouldReturnSucceedForValidAccessToken()
            throws AuthenticationException, AuthorizationException {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(accessTokenRetrieverMock.getStatusFromStorage()).thenReturn(AccessTokenStatus.VALID);

        // when
        final AuthenticationStepResponse returnedResponse =
                checkIfAccessTokenIsValidStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isTrue();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldReturnExecuteNextStepForExpiredAccessToken()
            throws AuthenticationException, AuthorizationException {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(accessTokenRetrieverMock.getStatusFromStorage()).thenReturn(AccessTokenStatus.EXPIRED);

        // when
        final AuthenticationStepResponse returnedResponse =
                checkIfAccessTokenIsValidStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldReturnExecuteNextStepAccessTokenIsNotPresent()
            throws AuthenticationException, AuthorizationException {
        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(accessTokenRetrieverMock.getStatusFromStorage())
                .thenReturn(AccessTokenStatus.NOT_PRESENT);

        // when
        final AuthenticationStepResponse returnedResponse =
                checkIfAccessTokenIsValidStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }
}
