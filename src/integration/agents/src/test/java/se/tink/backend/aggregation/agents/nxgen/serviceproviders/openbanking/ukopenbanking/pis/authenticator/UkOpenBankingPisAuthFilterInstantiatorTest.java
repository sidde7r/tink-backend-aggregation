package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createToken;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.AuthTokenCategory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.UkPisAuthToken;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class UkOpenBankingPisAuthFilterInstantiatorTest {

    private UkOpenBankingPisAuthFilterInstantiator authFilterInstantiator;

    private UkOpenBankingPisAuthApiClient apiClientMock;

    private UkOpenBankingPaymentStorage paymentStorageMock;

    @Before
    public void setUp() {
        apiClientMock = mock(UkOpenBankingPisAuthApiClient.class);
        final OpenIdAuthenticationValidator authenticationValidatorMock =
                mock(OpenIdAuthenticationValidator.class);

        paymentStorageMock = mock(UkOpenBankingPaymentStorage.class);

        authFilterInstantiator =
                new UkOpenBankingPisAuthFilterInstantiator(
                        apiClientMock, authenticationValidatorMock, paymentStorageMock);
    }

    @Test
    public void shouldInstantiateAuthFilterWithClientToken() {
        // given
        final OAuth2Token oAuth2Token = createToken();
        when(apiClientMock.requestClientCredentials()).thenReturn(oAuth2Token);
        when(paymentStorageMock.hasToken()).thenReturn(Boolean.FALSE);

        // when
        authFilterInstantiator.instantiateAuthFilterWithClientToken();

        // then
        verify(apiClientMock).requestClientCredentials();
        verify(paymentStorageMock, never()).getToken();

        final UkPisAuthToken expectedToken = createClientAuthToken(oAuth2Token);
        verify(paymentStorageMock).storeToken(expectedToken);
    }

    @Test
    public void shouldInstantiateAuthFilterWithClientTokenWhenValidTokenInStorage() {
        // given
        when(paymentStorageMock.hasToken()).thenReturn(Boolean.TRUE);

        final OAuth2Token oAuth2Token = createToken();
        when(oAuth2Token.isValid()).thenReturn(Boolean.TRUE);

        final UkPisAuthToken clientToken = createClientAuthToken(oAuth2Token);
        when(paymentStorageMock.getToken()).thenReturn(clientToken);

        // when
        authFilterInstantiator.instantiateAuthFilterWithClientToken();

        // then
        verify(apiClientMock, never()).requestClientCredentials();
        verify(paymentStorageMock).getToken();
        verify(paymentStorageMock, never()).storeToken(any());
    }

    @Test
    public void shouldInstantiateAuthFilterWithClientTokenWhenInvalidTokenInStorage() {
        // given
        when(paymentStorageMock.hasToken()).thenReturn(Boolean.TRUE);

        final OAuth2Token oAuth2Token = createToken();
        when(oAuth2Token.isValid()).thenReturn(Boolean.FALSE);

        final UkPisAuthToken clientToken = createClientAuthToken(oAuth2Token);
        when(paymentStorageMock.getToken()).thenReturn(clientToken);

        final OAuth2Token newOAuth2Token = createToken();
        when(apiClientMock.requestClientCredentials()).thenReturn(newOAuth2Token);

        // when
        authFilterInstantiator.instantiateAuthFilterWithClientToken();

        // then
        verify(apiClientMock).requestClientCredentials();
        verify(paymentStorageMock).getToken();

        final UkPisAuthToken expectedToken = createClientAuthToken(newOAuth2Token);
        verify(paymentStorageMock).storeToken(expectedToken);
    }

    private static UkPisAuthToken createClientAuthToken(OAuth2Token oAuth2Token) {
        return UkPisAuthToken.builder()
                .oAuth2Token(oAuth2Token)
                .tokenCategory(AuthTokenCategory.CLIENT_TOKEN)
                .build();
    }
}
