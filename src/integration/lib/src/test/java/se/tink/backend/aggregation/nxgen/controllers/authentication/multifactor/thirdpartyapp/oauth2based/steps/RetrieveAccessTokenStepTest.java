package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

public class RetrieveAccessTokenStepTest extends StepTestBase {

    private RetrieveAccessTokenStep retrieveAccessTokenStep;

    @Before
    public void setUp() {
        final AccessTokenFetcher accessTokenFetcherMock = mock(AccessTokenFetcher.class);

        retrieveAccessTokenStep = new RetrieveAccessTokenStep(accessTokenFetcherMock);
    }

    @Test
    public void shouldReturnSucceedIfTokenRetrievalWasSuccessful() {
        // when
        final AuthenticationStepResponse response =
                executeStepAndGetResponse(retrieveAccessTokenStep);

        // then
        assertThat(response.getNextStepId().isPresent()).isFalse();
        assertThat(response.isAuthenticationFinished()).isTrue();
    }
}
