package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;

public class Xs2aDevelopersAuthenticatorTest {
    private Xs2aDevelopersAuthenticator authenticatorController;
    private Xs2aDevelopersAuthenticatorHelper authenticator;
    private Xs2aDevelopersRedirectAuthenticator redirectAuthenticator;
    private Xs2aDevelopersDecoupledAuthenticatior decoupledAuthenticator;

    @Before
    public void init() {

        authenticator = mock(Xs2aDevelopersAuthenticatorHelper.class);
        redirectAuthenticator = mock(Xs2aDevelopersRedirectAuthenticator.class);
        decoupledAuthenticator = mock(Xs2aDevelopersDecoupledAuthenticatior.class);
        authenticatorController =
                new Xs2aDevelopersAuthenticator(
                        redirectAuthenticator, decoupledAuthenticator, authenticator);
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
        verify(decoupledAuthenticator).authenticate();
        verifyNoMoreInteractions(redirectAuthenticator);
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
        verify(redirectAuthenticator).authenticate(credentials);
        verifyNoMoreInteractions(decoupledAuthenticator);
    }

    @Test
    public void route_authentication_to_oauth2_if_username_is_null() {
        // given
        Credentials credentials = mock(Credentials.class);
        when(credentials.getField(Key.USERNAME)).thenReturn(null);

        // when
        authenticatorController.authenticate(credentials);

        // then
        verify(redirectAuthenticator).authenticate(credentials);
        verifyNoMoreInteractions(decoupledAuthenticator);
    }

    @Test
    public void route_auto_authentication_to_decoupled_if_possible() {
        // given
        when(authenticator.isDecoupledAuthenticationPossible()).thenReturn(true);

        // when
        authenticatorController.autoAuthenticate();

        // then
        verify(decoupledAuthenticator).autoAuthenticate();
        verifyNoMoreInteractions(redirectAuthenticator);
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
        verify(redirectAuthenticator).autoAuthenticate();
        verifyNoMoreInteractions(decoupledAuthenticator);
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
