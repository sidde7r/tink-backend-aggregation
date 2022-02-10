package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createToken;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.AuthTokenCategory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.entity.UkPisAuthToken;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.storage.UkOpenBankingPaymentStorage;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

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
    public void shouldInstantiateAuthFilterWithClientToken() throws PaymentException {
        // given
        final OAuth2Token oAuth2Token = createToken();
        when(apiClientMock.requestClientCredentials(ClientMode.PAYMENTS)).thenReturn(oAuth2Token);
        when(paymentStorageMock.hasToken()).thenReturn(Boolean.FALSE);

        // when
        authFilterInstantiator.instantiateAuthFilterWithClientToken();

        // then
        verify(apiClientMock).requestClientCredentials(ClientMode.PAYMENTS);
        verify(paymentStorageMock, never()).getToken();

        final UkPisAuthToken expectedToken = createClientAuthToken(oAuth2Token);
        verify(paymentStorageMock).storeToken(expectedToken);
    }

    @Test
    public void shouldThrowInvalidGrantExceptionWhenInstantiateAuthFilterFail() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        when(response.getBody(String.class)).thenReturn("invalid_grant");

        when(apiClientMock.requestClientCredentials(ClientMode.PAYMENTS))
                .thenThrow(new HttpResponseException(null, response));

        // when
        Throwable result =
                catchThrowable(authFilterInstantiator::instantiateAuthFilterWithClientToken);

        // then
        assertThat(result)
                .isExactlyInstanceOf(PaymentException.class)
                .hasFieldOrPropertyWithValue("InternalStatus", "INVALID_GRANT");
    }

    @Test
    public void shouldThrowPaymentExceptionWhenInstantiateAuthFilterFail() {
        // given
        HttpResponse response = mock(HttpResponse.class);
        when(response.getBody(String.class)).thenReturn("Internal Server Error");

        when(apiClientMock.requestClientCredentials(ClientMode.PAYMENTS))
                .thenThrow(new HttpResponseException(null, response));

        // when
        Throwable result =
                catchThrowable(authFilterInstantiator::instantiateAuthFilterWithClientToken);

        // then
        assertThat(result)
                .isExactlyInstanceOf(PaymentException.class)
                .hasFieldOrPropertyWithValue("InternalStatus", "PAYMENT_AUTHORIZATION_FAILED");
    }

    @Test
    public void shouldInstantiateAuthFilterWithClientTokenWhenValidTokenInStorage()
            throws PaymentException {
        // given
        when(paymentStorageMock.hasToken()).thenReturn(Boolean.TRUE);

        final OAuth2Token oAuth2Token = createToken();
        when(oAuth2Token.isValid()).thenReturn(Boolean.TRUE);

        final UkPisAuthToken clientToken = createClientAuthToken(oAuth2Token);
        when(paymentStorageMock.getToken()).thenReturn(clientToken);

        // when
        authFilterInstantiator.instantiateAuthFilterWithClientToken();

        // then
        verify(apiClientMock, never()).requestClientCredentials(ClientMode.PAYMENTS);
        verify(paymentStorageMock).getToken();
        verify(paymentStorageMock, never()).storeToken(any());
    }

    @Test
    public void shouldInstantiateAuthFilterWithClientTokenWhenInvalidTokenInStorage()
            throws PaymentException {
        // given
        when(paymentStorageMock.hasToken()).thenReturn(Boolean.TRUE);

        final OAuth2Token oAuth2Token = createToken();
        when(oAuth2Token.isValid()).thenReturn(Boolean.FALSE);

        final UkPisAuthToken clientToken = createClientAuthToken(oAuth2Token);
        when(paymentStorageMock.getToken()).thenReturn(clientToken);

        final OAuth2Token newOAuth2Token = createToken();
        when(apiClientMock.requestClientCredentials(ClientMode.PAYMENTS))
                .thenReturn(newOAuth2Token);

        // when
        authFilterInstantiator.instantiateAuthFilterWithClientToken();

        // then
        verify(apiClientMock).requestClientCredentials(ClientMode.PAYMENTS);
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
