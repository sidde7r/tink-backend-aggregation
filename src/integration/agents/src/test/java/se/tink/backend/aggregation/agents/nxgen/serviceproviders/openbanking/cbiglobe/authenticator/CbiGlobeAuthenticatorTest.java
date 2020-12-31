package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;

@RunWith(JUnitParamsRunner.class)
public class CbiGlobeAuthenticatorTest {

    private CbiGlobeAuthenticator authenticator;
    private CbiGlobeApiClient apiClient;
    private CbiUserState userState;
    private ConsentManager consentManager;

    @Before
    public void init() {
        apiClient = Mockito.mock(CbiGlobeApiClient.class);
        userState = Mockito.mock(CbiUserState.class);
        consentManager = Mockito.mock(ConsentManager.class);
        authenticator =
                new CbiGlobeAuthenticator(
                        apiClient,
                        Mockito.mock(StrongAuthenticationState.class),
                        userState,
                        consentManager,
                        Mockito.mock(CbiGlobeConfiguration.class));
    }

    @Test
    public void authenticationStepsShouldBeEmptyIfAutoAuthenticationIsPossible()
            throws SessionException {
        // given
        when(userState.isManualAuthenticationInProgress()).thenReturn(false);
        when(apiClient.isTokenValid()).thenReturn(true);
        when(consentManager.verifyIfConsentIsAccepted()).thenReturn(true);

        // when
        Iterable<AuthenticationStep> authenticationSteps = authenticator.authenticationSteps();

        // then
        assertThat(authenticationSteps).isEmpty();
    }

    @Test
    public void authenticationStepsShouldBuildStepsListIfAutoAuthenticationIsNotPossible()
            throws SessionException {
        // given
        when(userState.isManualAuthenticationInProgress()).thenReturn(true);
        when(apiClient.isTokenValid()).thenReturn(true);
        when(consentManager.verifyIfConsentIsAccepted()).thenReturn(true);

        // when
        List<AuthenticationStep> authenticationSteps =
                (List<AuthenticationStep>) authenticator.authenticationSteps();

        // then
        assertThat(authenticationSteps.size()).isEqualTo(3);
    }
}
