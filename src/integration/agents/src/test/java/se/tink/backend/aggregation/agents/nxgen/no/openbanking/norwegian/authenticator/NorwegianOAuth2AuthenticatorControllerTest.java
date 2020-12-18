package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;

public class NorwegianOAuth2AuthenticatorControllerTest {

    private static final String VALID = "valid";
    private static final String EXPIRED = "expired";
    private static final String REVOKED_BY_PSU = "revokedByPsu";

    private static final String OTHER_STATUS = "otherStatus";

    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2134, 5, 15, 10, 11, 36);

    private OAuth2AuthenticationController mockOAuth2AuthenticationController;
    private NorwegianAuthenticator mockNorwegianAuthenticator;

    private NorwegianOAuth2AuthenticatorController authenticatorController;

    @Before
    public void setup() {
        mockOAuth2AuthenticationController = mock(OAuth2AuthenticationController.class);
        mockNorwegianAuthenticator = mock(NorwegianAuthenticator.class);
        authenticatorController =
                new NorwegianOAuth2AuthenticatorController(
                        mockOAuth2AuthenticationController, mockNorwegianAuthenticator);
    }

    @Test
    public void shouldFinishAutoAuthSuccessfullyWhenConsentStillValid() {
        // given
        given(mockNorwegianAuthenticator.getPersistedConsentDetails())
                .willReturn(getConsentDetailsResponse(VALID));
        // when
        authenticatorController.autoAuthenticate();
        // then should finish up without errors

        verify(mockNorwegianAuthenticator).storeConsentValidUntil(TEST_TIME.toLocalDate());
    }

    @Test
    public void shouldThrowConsentExpiredWhenConsentExpired() {
        // given
        given(mockNorwegianAuthenticator.getPersistedConsentDetails())
                .willReturn(getConsentDetailsResponse(EXPIRED));
        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());
        // then should finish up without errors

        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.CONSENT_EXPIRED");
    }

    @Test
    public void shouldThrowConsentRevokedWhenConsentRevokedByPsu() {
        // given
        given(mockNorwegianAuthenticator.getPersistedConsentDetails())
                .willReturn(getConsentDetailsResponse(REVOKED_BY_PSU));
        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());
        // then should finish up without errors

        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.CONSENT_REVOKED_BY_USER");
    }

    @Test
    public void shouldThrowSessionExpiredWhenOtherStatus() {
        // given
        given(mockNorwegianAuthenticator.getPersistedConsentDetails())
                .willReturn(getConsentDetailsResponse(OTHER_STATUS));
        // when
        Throwable throwable = catchThrowable(() -> authenticatorController.autoAuthenticate());
        // then should finish up without errors

        assertThat(throwable)
                .isInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    private ConsentDetailsResponse getConsentDetailsResponse(String status) {
        return new ConsentDetailsResponse(TEST_TIME, status);
    }
}
