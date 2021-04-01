package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.entities.ConsentLinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Xs2aDevelopersAuthenticatorHelperTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers/resources/";
    private static final String EXPECTED_SCA_URL =
            "https://psd.xs2a-api.com/public/berlingroup/authorize/55d7b2c8-d120-441c-ab3c-ca930e2f6ec9";
    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String BASE_URL = "BASE_URL";
    private static final String REDIRECT_URL = "REDIRECT_URL";
    private static final Xs2aDevelopersProviderConfiguration xs2aDevelopersProviderConfiguration =
            new Xs2aDevelopersProviderConfiguration(CLIENT_ID, BASE_URL, REDIRECT_URL);
    private static LocalDateTimeSource localDateTimeSource;

    @Before
    public void init() {
        localDateTimeSource = mock(LocalDateTimeSource.class);
        when(localDateTimeSource.now()).thenReturn(LocalDateTime.of(1234, 5, 12, 12, 30, 40));
    }

    @Test
    public void getConsentResponse() {}

    @Test
    public void requestForConsent_should_store_consent_details_in_persistent_storage() {
        // given

        MultivaluedMap<String, String> headersMap = new MultivaluedMapImpl();
        headersMap.putSingle("ASPSP-SCA-Approach", "DECOUPLED");
        HttpResponse consentResponse = getConsentResponseHttpResponse(headersMap);

        PersistentStorage persistentStorage = new PersistentStorage();
        Credentials credentials = mock(Credentials.class);
        Xs2aDevelopersAuthenticatorHelper authenticator =
                createXs2aDevelopersAuthenticatorHelper(
                        persistentStorage, credentials, consentResponse);
        // when
        authenticator.requestForConsent();

        // then
        assertThat(persistentStorage.get(StorageKeys.CONSENT_ID)).isEqualTo("1604575204-ba78d90");
        assertThat(
                        persistentStorage
                                .get(StorageKeys.LINKS, ConsentLinksEntity.class)
                                .get()
                                .getScaOAuth())
                .isEqualTo(EXPECTED_SCA_URL);
        assertThat(persistentStorage.get(StorageKeys.SCA_APPROACH))
                .isEqualTo(StorageValues.DECOUPLED_APPROACH);
    }

    @Test
    public void
            requestForConsent_should_store_consent_details_wo_sca_approach_info_in_persistent_storage_if_not_available() {
        // given
        MultivaluedMap<String, String> headersMap = new MultivaluedMapImpl();
        HttpResponse consentResponse = getConsentResponseHttpResponse(headersMap);

        PersistentStorage persistentStorage = new PersistentStorage();
        Credentials credentials = mock(Credentials.class);
        Xs2aDevelopersAuthenticatorHelper authenticator =
                createXs2aDevelopersAuthenticatorHelper(
                        persistentStorage, credentials, consentResponse);
        // when
        authenticator.requestForConsent();

        // then
        assertThat(persistentStorage.get(StorageKeys.CONSENT_ID)).isEqualTo("1604575204-ba78d90");
        assertThat(
                        persistentStorage
                                .get(StorageKeys.LINKS, ConsentLinksEntity.class)
                                .get()
                                .getScaOAuth())
                .isEqualTo(EXPECTED_SCA_URL);
        assertThat(persistentStorage.get(StorageKeys.SCA_APPROACH)).isNull();
    }

    @Test
    public void storeConsentDetails_should_set_expiry_date() {
        // given
        PersistentStorage persistentStorage = mock(PersistentStorage.class);
        when(persistentStorage.get(StorageKeys.CONSENT_ID)).thenReturn("dummyConsentId");
        Credentials credentials = new Credentials();
        credentials.setField(Key.USERNAME, "dummyUsername");
        Xs2aDevelopersAuthenticatorHelper authenticator =
                createXs2aDevelopersAuthenticatorHelper(
                        persistentStorage, credentials, mock(HttpResponse.class));
        Date date = toDate("2030-01-01");
        // when
        authenticator.storeConsentDetails();

        // then
        assertThat(credentials.getSessionExpiryDate()).isEqualTo(date);
    }

    private Xs2aDevelopersAuthenticatorHelper createXs2aDevelopersAuthenticatorHelper(
            PersistentStorage persistentStorage,
            Credentials credentials,
            HttpResponse consentResponse) {
        TinkHttpClient httpClient = mockHttpClient(consentResponse);
        Xs2aDevelopersApiClient xs2aDevelopersApiClient =
                new Xs2aDevelopersApiClient(
                        httpClient,
                        persistentStorage,
                        xs2aDevelopersProviderConfiguration,
                        true,
                        "userIp",
                        new MockRandomValueGenerator());
        return new Xs2aDevelopersAuthenticatorHelper(
                xs2aDevelopersApiClient,
                persistentStorage,
                xs2aDevelopersProviderConfiguration,
                localDateTimeSource,
                credentials);
    }

    public TinkHttpClient mockHttpClient(HttpResponse consentResponse) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(tinkHttpClient.request(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.headers(any(Map.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.post(HttpResponse.class)).thenReturn(consentResponse);
        when(requestBuilder.post(TokenResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "token_response.json").toFile(),
                                TokenResponse.class));
        when(requestBuilder.get(ConsentDetailsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consent_details_response.json").toFile(),
                                ConsentDetailsResponse.class));
        return tinkHttpClient;
    }

    private HttpResponse getConsentResponseHttpResponse(MultivaluedMap<String, String> headersMap) {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getBody(ConsentResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "thirdparty_callback_response.json")
                                        .toFile(),
                                ConsentResponse.class));
        when(response.getHeaders()).thenReturn(headersMap);
        return response;
    }

    private Date toDate(String date) {
        return Date.from(
                LocalDate.parse(date).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
