package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheHeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.DeutscheBankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DeutscheBankAuthenticatorTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/deutschebank/resources/";

    private DeutscheBankApiClient deutscheBankApiClient;
    private DeutscheBankAuthenticator deutscheBankAuthenticator;
    private Credentials credentials;
    private PersistentStorage persistentStorage = new PersistentStorage();

    @Before
    public void setup() {
        persistentStorage = new PersistentStorage();
        deutscheBankApiClient = mock(DeutscheBankApiClient.class);
        credentials = new Credentials();
        deutscheBankAuthenticator =
                new DeutscheBankAuthenticator(
                        deutscheBankApiClient, persistentStorage, credentials);
    }

    @Test
    public void shouldConfirmValidStatus() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString(
                        "{\"consentStatus\": \"valid\"}", ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::verifyPersistedConsentIdIsValid);

        // Then
        assertThat(t).doesNotThrowAnyException();
    }

    @Test
    public void shouldThrowLoginErrorWhenExpiredStatus() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString(
                        "{\"consentStatus\": \"expired\"}", ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::verifyPersistedConsentIdIsValid);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void shouldThrowLoginErrorWhenStatusReceived() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString(
                        "{\"consentStatus\": \"received\"}", ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::verifyPersistedConsentIdIsValid);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void shouldThrowLoginErrorWhenErrorMessage() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString(
                        "{\"tppMessages\":[{\"code\":\"CONSENT_INVALID\",\"text\":\"Test.\",\"category\":\"ERROR\"}],\"transactionStatus\":\"RJCT\"}\n",
                        ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::verifyPersistedConsentIdIsValid);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void shouldThrowLoginErrorWhenStatusResponseIsNull() {
        // Given
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(null);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::verifyPersistedConsentIdIsValid);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void shouldThrowLoginErrorWhenStatusResponseIsEmpty() {
        // Given
        ConsentStatusResponse expected =
                SerializationUtils.deserializeFromString(
                        "{\"consentStatus\":\"expired\"}\n", ConsentStatusResponse.class);
        when(deutscheBankApiClient.getConsentStatus()).thenReturn(expected);

        // When
        Throwable t = catchThrowable(deutscheBankAuthenticator::verifyPersistedConsentIdIsValid);

        // Then
        assertThat(t)
                .isInstanceOf(LoginException.class)
                .hasMessage(LoginError.CREDENTIALS_VERIFICATION_ERROR.exception().getMessage());
    }

    @Test
    public void shouldStoreConsentIdInPersistentStorageAfterAuthentication() {
        // given
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        mockGetConsentRequest(tinkHttpClient);
        createBankAuthenticator(tinkHttpClient);

        // when
        deutscheBankAuthenticator.authenticate("state");

        // then
        assertThat(persistentStorage.get(StorageKeys.CONSENT_ID)).isEqualTo("consentId");
    }

    @Test
    public void shouldSetSessionExpiryAfterStoringSessingExpiry() {
        // given
        Date date =
                Date.from(
                        LocalDate.parse("2030-01-01")
                                .atStartOfDay()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
        persistentStorage.put(StorageKeys.CONSENT_ID, "consentId");

        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        mockGetConsentDetailsRequest(tinkHttpClient);
        createBankAuthenticator(tinkHttpClient);

        // when
        deutscheBankAuthenticator.storeSessionExpiry();

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(date);
    }

    private void mockGetConsentRequest(TinkHttpClient tinkHttpClient) {
        RequestBuilder requestBuilder = mockBaseRequestBuilderCalls(tinkHttpClient);

        when(requestBuilder.post(any(), any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(TEST_DATA_PATH + "consent_response.json"),
                                ConsentResponse.class));
    }

    private void mockGetConsentDetailsRequest(TinkHttpClient tinkHttpClient) {
        RequestBuilder requestBuilder = mockBaseRequestBuilderCalls(tinkHttpClient);

        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(TEST_DATA_PATH + "consent_detail_response.json"),
                                ConsentDetailsResponse.class));
    }

    private RequestBuilder mockBaseRequestBuilderCalls(TinkHttpClient tinkHttpClient) {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.type(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.accept(anyString())).thenReturn(requestBuilder);
        return requestBuilder;
    }

    private void createBankAuthenticator(TinkHttpClient tinkHttpClient) {
        DeutscheMarketConfiguration deutscheMarketConfiguration =
                new DeutscheMarketConfiguration("baseUrl", "psuIdType");
        deutscheBankApiClient =
                new DeutscheBankApiClient(
                        tinkHttpClient,
                        persistentStorage,
                        new DeutscheHeaderValues("redirectUrl", "userIp"),
                        deutscheMarketConfiguration);
        deutscheBankAuthenticator =
                new DeutscheBankAuthenticator(
                        deutscheBankApiClient, persistentStorage, credentials);
    }
}
