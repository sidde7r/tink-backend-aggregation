package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;

public class Xs2aDevelopersAuthenticatorControllerTest {
    private Xs2aDevelopersAuthenticatorController authenticatorController;
    private Xs2aDevelopersAuthenticator authenticator;
    private Xs2aDevelopersOAuth2AuthenticatorController oAuth2AuthenticatorController;
    private Xs2aDevelopersDecoupledAuthenticationController decoupledAuthenticationController;

    @Before
    public void init() {
        authenticator = mock(Xs2aDevelopersAuthenticator.class);
        oAuth2AuthenticatorController = mock(Xs2aDevelopersOAuth2AuthenticatorController.class);
        decoupledAuthenticationController =
                mock(Xs2aDevelopersDecoupledAuthenticationController.class);
        authenticatorController =
                new Xs2aDevelopersAuthenticatorController(
                        oAuth2AuthenticatorController,
                        decoupledAuthenticationController,
                        authenticator);
    }

    @Test
    public void route_authentication_to_decoupled_if_possible() {
        // given
        Credentials credentials = mock(Credentials.class);
        when(credentials.getField(Key.USERNAME)).thenReturn("dummyUsername");
        when(authenticator.isDecoupledAuthenticationPossible()).thenReturn(true);

        // when
        authenticatorController.authenticate(credentials);

        // then
        verify(decoupledAuthenticationController).authenticate();
        verifyNoMoreInteractions(oAuth2AuthenticatorController);
    }

    @Test
    public void route_authentication_to_oauth2_if_decoupled_not_possible() {
        // given
        Credentials credentials = mock(Credentials.class);
        when(credentials.getField(Key.USERNAME)).thenReturn("dummyUsername");
        when(authenticator.isDecoupledAuthenticationPossible()).thenReturn(false);

        // when
        authenticatorController.authenticate(credentials);

        // then
        verify(oAuth2AuthenticatorController).authenticate(credentials);
        verifyNoMoreInteractions(decoupledAuthenticationController);
    }

    @Test
    public void route_authentication_to_oauth2_if_username_is_null() {
        // given
        Credentials credentials = mock(Credentials.class);
        when(credentials.getField(Key.USERNAME)).thenReturn(null);

        // when
        authenticatorController.authenticate(credentials);

        // then
        verify(oAuth2AuthenticatorController).authenticate(credentials);
        verifyNoMoreInteractions(decoupledAuthenticationController);
    }

    @Test
    public void route_auto_authentication_to_decoupled_if_possible() {
        // given
        when(authenticator.isDecoupledAuthenticationPossible()).thenReturn(true);

        // when
        authenticatorController.autoAuthenticate();

        // then
        verify(decoupledAuthenticationController).autoAuthenticate();
        verifyNoMoreInteractions(oAuth2AuthenticatorController);
    }

    @Test
    public void route_auto_authentication_to_oauth2_if_decoupled_not_possible() {
        // given
        Credentials credentials = mock(Credentials.class);
        when(credentials.getField(Key.USERNAME)).thenReturn("dummyUsername");
        when(authenticator.isDecoupledAuthenticationPossible()).thenReturn(false);

        // when
        authenticatorController.autoAuthenticate();

        // then
        verify(oAuth2AuthenticatorController).autoAuthenticate();
        verifyNoMoreInteractions(decoupledAuthenticationController);
    }

    @Test
    public void when_user_is_authenticated_then_session_expiry_date_is_set() {
        // given
        Credentials credentials = mock(Credentials.class);

        // when
        authenticatorController.authenticate(credentials);

        // then
        verify(authenticator).storeConsentDetails();
    }

    @Test
    public void when_autoAuthenticate_then_session_expiry_date_should_be_set() {
        // given
        // when
        authenticatorController.autoAuthenticate();

        // then
        verify(authenticator).storeConsentDetails();
    }
}
