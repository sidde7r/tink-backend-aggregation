package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.TestFixtures.givenKeyPairMock;

import java.security.KeyPair;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.ConfigurationProvider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.scaffold.ExternalApiCallResult;

public class HVBAuthenticatorTest {

    private HVBStorage storage = mock(HVBStorage.class);
    private ConfigurationProvider configurationProvider = mock(ConfigurationProvider.class);
    private DataEncoder dataEncoder = mock(DataEncoder.class);
    private LocalDateTimeSource localDateTimeSource = mock(LocalDateTimeSource.class);

    private RegistrationCall registrationRequest = mock(RegistrationCall.class);
    private PreAuthorizationCall preAuthorizationRequest = mock(PreAuthorizationCall.class);
    private AuthorizationCall authorizationRequest = mock(AuthorizationCall.class);
    private AccessTokenCall accessTokenCall = mock(AccessTokenCall.class);

    private HVBAuthenticator tested =
            new HVBAuthenticator(
                    storage,
                    configurationProvider,
                    dataEncoder,
                    localDateTimeSource,
                    registrationRequest,
                    preAuthorizationRequest,
                    authorizationRequest,
                    accessTokenCall);

    @Test
    public void authenticateShouldPerformThrowExceptionForMissingCredentialFields() {
        // given
        Credentials givenCredentials = new Credentials();

        // when
        Throwable throwable = catchThrowable(() -> tested.authenticate(givenCredentials));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");
    }

    @Test
    public void authenticateShouldPerformRegistrationFlowForNewUser()
            throws AuthenticationException, AuthorizationException {
        // given
        when(storage.getClientId()).thenReturn(null);
        when(dataEncoder.base64Encode(any())).thenReturn("basedString");

        KeyPair givenKeyPair = givenKeyPairMock();
        when(configurationProvider.getPredefinedRsaKeyPair()).thenReturn(givenKeyPair);
        when(storage.getKeyPair()).thenReturn(givenKeyPair);

        String givenUsername = "username";
        String givenPin = "pin";
        Credentials givenCredentials = new Credentials();
        givenCredentials.setField(Field.Key.USERNAME, givenUsername);
        givenCredentials.setField(Key.PASSWORD, givenPin);

        when(registrationRequest.execute(any())).thenReturn(ExternalApiCallResult.of("", 200));
        when(preAuthorizationRequest.execute(any())).thenReturn(ExternalApiCallResult.of("", 200));
        when(authorizationRequest.execute(any())).thenReturn(ExternalApiCallResult.of("", 200));
        when(accessTokenCall.execute(any()))
                .thenReturn(ExternalApiCallResult.of(new AccessTokenResponse(), 200));

        // when
        tested.authenticate(givenCredentials);

        // then
        verify(registrationRequest).execute(any());
        verify(preAuthorizationRequest).execute(any());
        verify(authorizationRequest).execute(any());
        verify(accessTokenCall).execute(any());
        verify(storage).setAccessToken(any());
    }

    @Test
    public void authenticateShouldThrowProperExceptionForUnsuccessfulRegistration() {
        // given
        when(storage.getClientId()).thenReturn(null);
        when(dataEncoder.base64Encode(any())).thenReturn("basedString");

        KeyPair givenKeyPair = givenKeyPairMock();
        when(configurationProvider.getPredefinedRsaKeyPair()).thenReturn(givenKeyPair);
        when(storage.getKeyPair()).thenReturn(givenKeyPair);

        String givenUsername = "username";
        String givenPin = "pin";
        Credentials givenCredentials = new Credentials();
        givenCredentials.setField(Field.Key.USERNAME, givenUsername);
        givenCredentials.setField(Key.PASSWORD, givenPin);

        when(registrationRequest.execute(any())).thenReturn(ExternalApiCallResult.of("", 400));

        // when
        Throwable throwable = catchThrowable(() -> tested.authenticate(givenCredentials));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.REGISTER_DEVICE_ERROR");

        verify(registrationRequest).execute(any());
        verify(preAuthorizationRequest, never()).execute(any());
        verify(authorizationRequest, never()).execute(any());
        verify(accessTokenCall, never()).execute(any());
        verify(storage, never()).setAccessToken(any());
    }

    @Test
    public void authenticateShouldPerformAuthenticationFlowForRegisteredUser()
            throws AuthenticationException, AuthorizationException {
        // given
        when(storage.getClientId()).thenReturn("registeredClientId");

        KeyPair givenKeyPair = givenKeyPairMock();
        when(storage.getKeyPair()).thenReturn(givenKeyPair);

        String givenUsername = "username";
        String givenPin = "pin";
        Credentials givenCredentials = new Credentials();
        givenCredentials.setField(Field.Key.USERNAME, givenUsername);
        givenCredentials.setField(Key.PASSWORD, givenPin);

        when(preAuthorizationRequest.execute(any())).thenReturn(ExternalApiCallResult.of("", 200));
        when(authorizationRequest.execute(any())).thenReturn(ExternalApiCallResult.of("", 200));
        when(accessTokenCall.execute(any()))
                .thenReturn(ExternalApiCallResult.of(new AccessTokenResponse(), 200));

        // when
        tested.authenticate(givenCredentials);

        // then
        verify(registrationRequest, never()).execute(any());

        verify(preAuthorizationRequest).execute(any());
        verify(authorizationRequest).execute(any());
        verify(accessTokenCall).execute(any());
        verify(storage).setAccessToken(any());
    }

    @Test
    public void authenticateShouldThrowProperExceptionForUnsuccessfulAuthorization() {
        // given
        when(storage.getClientId()).thenReturn("registeredClientId");

        KeyPair givenKeyPair = givenKeyPairMock();
        when(storage.getKeyPair()).thenReturn(givenKeyPair);

        String givenUsername = "username";
        String givenPin = "pin";
        Credentials givenCredentials = new Credentials();
        givenCredentials.setField(Field.Key.USERNAME, givenUsername);
        givenCredentials.setField(Key.PASSWORD, givenPin);

        when(preAuthorizationRequest.execute(any())).thenReturn(ExternalApiCallResult.of("", 200));
        when(authorizationRequest.execute(any())).thenReturn(ExternalApiCallResult.of("", 400));
        when(accessTokenCall.execute(any()))
                .thenReturn(ExternalApiCallResult.of(new AccessTokenResponse(), 200));

        // when
        Throwable throwable = catchThrowable(() -> tested.authenticate(givenCredentials));

        // then
        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CREDENTIALS");

        verify(registrationRequest, never()).execute(any());

        verify(preAuthorizationRequest).execute(any());
        verify(authorizationRequest).execute(any());
        verify(accessTokenCall, never()).execute(any());
        verify(storage, never()).setAccessToken(any());
    }
}
