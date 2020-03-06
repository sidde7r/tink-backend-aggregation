package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStatus;

public class CheckIfAccessTokenIsValidStepTest extends StepTestBase {

    private CheckIfAccessTokenIsValidStep checkIfAccessTokenIsValidStep;

    private AccessTokenFetcher accessTokenFetcherMock;

    @Before
    public void setUp() {
        accessTokenFetcherMock = mock(AccessTokenFetcher.class);

        checkIfAccessTokenIsValidStep = new CheckIfAccessTokenIsValidStep(accessTokenFetcherMock);
    }

    @Test
    public void shouldReturnSucceedIfTokenIsValid() {
        // given
        when(accessTokenFetcherMock.getAccessTokenStatus()).thenReturn(AccessTokenStatus.VALID);

        // when
        final AuthenticationStepResponse response =
                executeStepAndGetResponse(checkIfAccessTokenIsValidStep);

        // then
        assertThat(response.getNextStepId().isPresent()).isFalse();
        assertThat(response.isAuthenticationFinished()).isTrue();
    }

    @Test
    public void shouldReturnExecuteNextStepIfTokenIExpired() {
        // given
        when(accessTokenFetcherMock.getAccessTokenStatus()).thenReturn(AccessTokenStatus.EXPIRED);

        // when
        final AuthenticationStepResponse response =
                executeStepAndGetResponse(checkIfAccessTokenIsValidStep);

        // then
        assertThat(response.getNextStepId().isPresent()).isFalse();
        assertThat(response.isAuthenticationFinished()).isFalse();
    }

    @Test
    public void shouldReturnExecuteThirdPartyAppStepIfTokenIExpiredAndCannotBeRefreshed() {
        // given
        when(accessTokenFetcherMock.getAccessTokenStatus())
                .thenReturn(AccessTokenStatus.NOT_PRESENT);

        // when
        final AuthenticationStepResponse response =
                executeStepAndGetResponse(checkIfAccessTokenIsValidStep);

        // then
        assertThat(response.getNextStepId().isPresent()).isTrue();
        assertThat(response.getNextStepId().get())
                .isEqualTo(ThirdPartyAppAuthenticationStepCreator.STEP_NAME);
        assertThat(response.isAuthenticationFinished()).isFalse();
    }
}
