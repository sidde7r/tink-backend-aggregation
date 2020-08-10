package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class KbcApiClientTest {

    private static final String BASE_URL = "https://base-url";
    private static final String REDIRECT_URL = "https://redirect-url";
    private static final String CONSENT_URL = BASE_URL + "/psd2/v2/consents";
    private static final String PSU_IP_ADDR = "0.0.0.0";
    private static final String CONSENT_ID = "1234";
    private static final String QSEALC = "QSEALC";

    private KbcApiClient kbcApiClient;
    private Credentials credentialsMock;

    @Before
    public void setUp() {
        final TinkHttpClient httpClientMock = mock(TinkHttpClient.class);
        final PersistentStorage persistentStorageMock = mock(PersistentStorage.class);
        final KbcConfiguration kbcConfigurationMock = mock(KbcConfiguration.class);

        when(kbcConfigurationMock.getBaseUrl()).thenReturn(BASE_URL);
        when(kbcConfigurationMock.getPsuIpAddress()).thenReturn(PSU_IP_ADDR);

        setUpHttpClientMock(httpClientMock);

        credentialsMock = mock(Credentials.class);

        kbcApiClient =
                new KbcApiClient(
                        httpClientMock,
                        kbcConfigurationMock,
                        REDIRECT_URL,
                        credentialsMock,
                        persistentStorageMock,
                        QSEALC);
    }

    @Test
    public void shouldGetConsentIdForCorrectIban() {
        // given
        final String dummyIban = "BE75846503538653";
        when(credentialsMock.getField(KbcConstants.CredentialKeys.IBAN)).thenReturn(dummyIban);

        // when
        final String returnedConsentId = kbcApiClient.getConsentId();

        // then
        assertThat(returnedConsentId).isEqualTo(CONSENT_ID);
    }

    @Test
    public void shouldThrowExceptionForNullIban() {
        // given
        when(credentialsMock.getField(KbcConstants.CredentialKeys.IBAN)).thenReturn(null);

        // when
        final Throwable thrown = catchThrowable(kbcApiClient::getConsentId);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Iban has incorrect format.");
    }

    @Test
    public void shouldThrowExceptionForEmptyIban() {
        // given
        when(credentialsMock.getField(KbcConstants.CredentialKeys.IBAN)).thenReturn("");

        // when
        final Throwable thrown = catchThrowable(kbcApiClient::getConsentId);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Iban has incorrect format.");
    }

    @Test
    public void shouldThrowExceptionWhenIbanStartsWithLowerCase() {
        // given
        final String dummyIban = "be75846503538653";
        when(credentialsMock.getField(KbcConstants.CredentialKeys.IBAN)).thenReturn(dummyIban);

        // when
        final Throwable thrown = catchThrowable(kbcApiClient::getConsentId);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Iban has incorrect format.");
    }

    @Test
    public void shouldThrowExceptionWhenIbanIsTooShort() {
        // given
        final String dummyIban = "BE7584650353865";
        when(credentialsMock.getField(KbcConstants.CredentialKeys.IBAN)).thenReturn(dummyIban);

        // when
        final Throwable thrown = catchThrowable(kbcApiClient::getConsentId);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Iban has incorrect format.");
    }

    @Test
    public void shouldThrowExceptionWhenIbanIsTooLong() {
        // given
        final String dummyIban = "BE758465035386538";
        when(credentialsMock.getField(KbcConstants.CredentialKeys.IBAN)).thenReturn(dummyIban);

        // when
        final Throwable thrown = catchThrowable(kbcApiClient::getConsentId);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Iban has incorrect format.");
    }

    @Test
    public void shouldThrowExceptionWhenIbanDoesNotFollowFormat() {
        // given
        final String dummyIban = "BE758465C353865";
        when(credentialsMock.getField(KbcConstants.CredentialKeys.IBAN)).thenReturn(dummyIban);

        // when
        final Throwable thrown = catchThrowable(kbcApiClient::getConsentId);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Iban has incorrect format.");
    }

    @Test
    public void shouldThrowExceptionWhenIbanHasSpace() {
        // given
        final String dummyIban = "BE758465 353865";
        when(credentialsMock.getField(KbcConstants.CredentialKeys.IBAN)).thenReturn(dummyIban);

        // when
        final Throwable thrown = catchThrowable(kbcApiClient::getConsentId);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Iban has incorrect format.");
    }

    private static void setUpHttpClientMock(TinkHttpClient httpClientMock) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.body(anyString(), any(MediaType.class)))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header("TPP-Redirect-Uri", REDIRECT_URL))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header("PSU-IP-Address", PSU_IP_ADDR))
                .thenReturn(requestBuilderMock);
        when(requestBuilderMock.header(eq("X-Request-ID"), any(UUID.class)))
                .thenReturn(requestBuilderMock);

        final ConsentBaseResponse consentBaseResponseMock = mock(ConsentBaseResponse.class);
        when(consentBaseResponseMock.getConsentId()).thenReturn(CONSENT_ID);

        when(requestBuilderMock.post(ConsentBaseResponse.class))
                .thenReturn(consentBaseResponseMock);

        when(httpClientMock.request(CONSENT_URL)).thenReturn(requestBuilderMock);
    }
}
