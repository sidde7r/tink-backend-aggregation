package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class ChebancaAuthenticationControllerTest {
    private OAuth2AuthenticationController oAuth2AuthenticationController;
    private ChebancaBgAutoAuthenticator bgAutoAuthenticator;
    private ChebancaConsentManager consentManager;
    private SupplementalInformationHelper supplementalInformationHelper;

    @Before
    public void init() {
        consentManager = mock(ChebancaConsentManager.class);
        bgAutoAuthenticator = mock(ChebancaBgAutoAuthenticator.class);
        oAuth2AuthenticationController = mock(OAuth2AuthenticationController.class);
        supplementalInformationHelper = mock(SupplementalInformationHelper.class);
    }

    @Test
    public void autoAuthenticateShouldRouteToOAuth2AuthenticationControllerIfUserPresent() {
        // given
        boolean isUserPresent = true;
        when(consentManager.isConsentValid()).thenReturn(true);
        ChebancaAuthenticationController authenticationController =
                new ChebancaAuthenticationController(
                        oAuth2AuthenticationController,
                        consentManager,
                        supplementalInformationHelper,
                        bgAutoAuthenticator,
                        isUserPresent);

        // when
        authenticationController.autoAuthenticate();

        // then
        verify(oAuth2AuthenticationController).autoAuthenticate();
        verifyZeroInteractions(bgAutoAuthenticator);
    }

    @Test
    public void autoAuthenticateShouldRouteToBackgroundAutoAuthenticatorIfUserNotPresent() {
        // given
        boolean isUserPresent = false;
        when(consentManager.isConsentValid()).thenReturn(true);
        ChebancaAuthenticationController authenticationController =
                new ChebancaAuthenticationController(
                        oAuth2AuthenticationController,
                        consentManager,
                        supplementalInformationHelper,
                        bgAutoAuthenticator,
                        isUserPresent);

        // when
        authenticationController.autoAuthenticate();

        // then
        verify(bgAutoAuthenticator).autoAuthenticate();
        verifyZeroInteractions(oAuth2AuthenticationController);
    }

    @Test
    public void autoAuthenticateShouldThrowSessionExceptionIfConsentNotValid() {
        // given
        boolean isUserPresent = true;
        when(consentManager.isConsentValid()).thenReturn(false);
        ChebancaAuthenticationController authenticationController =
                new ChebancaAuthenticationController(
                        oAuth2AuthenticationController,
                        consentManager,
                        supplementalInformationHelper,
                        bgAutoAuthenticator,
                        isUserPresent);

        // when
        Throwable throwable = catchThrowable(authenticationController::autoAuthenticate);

        // then
        assertThat(throwable).isInstanceOf(SessionException.class);
    }
}
