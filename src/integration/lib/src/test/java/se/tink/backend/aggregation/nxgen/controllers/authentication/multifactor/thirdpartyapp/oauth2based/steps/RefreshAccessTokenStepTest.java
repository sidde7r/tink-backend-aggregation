package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenRefreshStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class RefreshAccessTokenStepTest extends StepTestBase {

    private RefreshAccessTokenStep refreshAccessTokenStep;

    private AccessTokenFetcher accessTokenFetcherMock;

    @Before
    public void setUp() {
        accessTokenFetcherMock = mock(AccessTokenFetcher.class);

        refreshAccessTokenStep = new RefreshAccessTokenStep(accessTokenFetcherMock);
    }

    @Test
    public void shouldReturnSucceedIfTokenRefreshWasSuccessful() {
        // given
        when(accessTokenFetcherMock.refreshAccessToken())
                .thenReturn(AccessTokenRefreshStatus.SUCCESS);

        // when
        final AuthenticationStepResponse response =
                executeStepAndGetResponse(refreshAccessTokenStep);

        // then
        assertThat(response.getNextStepId().isPresent()).isFalse();
        assertThat(response.isAuthenticationFinished()).isTrue();
    }

    @Test
    public void shouldReturnExecuteThirdPartyAppStepIfTokenRefreshWasNotSuccessful() {
        // given
        when(accessTokenFetcherMock.refreshAccessToken())
                .thenReturn(AccessTokenRefreshStatus.FAILED);

        // when
        final AuthenticationStepResponse response =
                executeStepAndGetResponse(refreshAccessTokenStep);

        // then
        assertThat(response.getNextStepId().isPresent()).isTrue();
        assertThat(response.getNextStepId().get())
                .isEqualTo(ThirdPartyAppAuthenticationStepCreator.STEP_NAME);
        assertThat(response.isAuthenticationFinished()).isFalse();
    }
}
