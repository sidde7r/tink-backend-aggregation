package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Xs2aDevelopersDecoupledAuthenticationControllerTest {
    private Credentials credentials;
    private SupplementalInformationController supplementalInformationController;
    private SupplementalInformationFormer supplementalInformationFormer;

    private static final String CLIENT_ID = "CLIENT_ID";
    private static final String BASE_URL = "BASE_URL";
    private static final String REDIRECT_URL = "REDIRECT_URL";
    private static final Xs2aDevelopersProviderConfiguration xs2aDevelopersProviderConfiguration =
            new Xs2aDevelopersProviderConfiguration(CLIENT_ID, BASE_URL, REDIRECT_URL);
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/xs2adevelopers/resources/";

    @Before
    public void init() {
        supplementalInformationController = mock(SupplementalInformationController.class);
        supplementalInformationFormer = mock(SupplementalInformationFormer.class);
        credentials = new Credentials();
    }

    private Xs2aDevelopersAuthenticator createXs2aDevelopersAuthenticator(
            TinkHttpClient tinkHttpClient,
            PersistentStorage persistentStorage,
            Credentials credentials) {

        LocalDateTimeSource localDateTimeSource = mock(LocalDateTimeSource.class);
        when(localDateTimeSource.now()).thenReturn(LocalDateTime.of(1234, 5, 12, 12, 30, 40));

        Xs2aDevelopersApiClient xs2aDevelopersApiClient =
                new Xs2aDevelopersApiClient(
                        tinkHttpClient,
                        persistentStorage,
                        xs2aDevelopersProviderConfiguration,
                        true,
                        "userIp",
                        new MockRandomValueGenerator());
        return new Xs2aDevelopersAuthenticator(
                xs2aDevelopersApiClient,
                persistentStorage,
                xs2aDevelopersProviderConfiguration,
                localDateTimeSource,
                credentials);
    }

    @Test
    public void authenticate_should_not_throw_exception_if_consent_is_accepted() {
        // given
        RequestBuilder requestBuilder = mockRequestBuilder();
        when(requestBuilder.get(ConsentStatusResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consent_status_response.json").toFile(),
                                ConsentStatusResponse.class));
        TinkHttpClient httpClient = mockHttpClient(requestBuilder);

        Xs2aDevelopersAuthenticator authenticator =
                createXs2aDevelopersAuthenticator(
                        httpClient, createPersistentStorage(), credentials);
        Xs2aDevelopersDecoupledAuthenticationController decoupledAuthenticationController =
                new Xs2aDevelopersDecoupledAuthenticationController(
                        authenticator,
                        supplementalInformationController,
                        supplementalInformationFormer);

        // when
        Throwable throwable =
                catchThrowable(() -> decoupledAuthenticationController.authenticate());

        // then
        assertThat(throwable).isNull();
        assertThat(credentials.getSessionExpiryDate()).isNotNull();
    }

    @Test
    public void authenticate_should_not_throw_exception_if_consent_status_failed() {
        // given
        RequestBuilder requestBuilder = mockRequestBuilder();
        when(requestBuilder.get(ConsentStatusResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "failed_consent_status_response.json")
                                        .toFile(),
                                ConsentStatusResponse.class));
        TinkHttpClient httpClient = mockHttpClient(requestBuilder);

        Xs2aDevelopersAuthenticator authenticator =
                createXs2aDevelopersAuthenticator(
                        httpClient, createPersistentStorage(), credentials);
        Xs2aDevelopersDecoupledAuthenticationController decoupledAuthenticationController =
                new Xs2aDevelopersDecoupledAuthenticationController(
                        authenticator,
                        supplementalInformationController,
                        supplementalInformationFormer);

        // when
        Throwable throwable =
                catchThrowable(() -> decoupledAuthenticationController.authenticate());

        // then
        assertThat(throwable).isInstanceOf(ThirdPartyAppException.class);
    }

    @Test
    public void autoAuthenticate_should_throw_sessionError_if_consent_is_invalid() {
        // given
        RequestBuilder requestBuilder = mockRequestBuilder();
        when(requestBuilder.get(ConsentStatusResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "invalid_consent_status_response.json")
                                        .toFile(),
                                ConsentStatusResponse.class));
        TinkHttpClient httpClient = mockHttpClient(requestBuilder);

        Xs2aDevelopersAuthenticator authenticator =
                createXs2aDevelopersAuthenticator(
                        httpClient, createPersistentStorage(), credentials);
        Xs2aDevelopersDecoupledAuthenticationController decoupledAuthenticationController =
                new Xs2aDevelopersDecoupledAuthenticationController(
                        authenticator,
                        supplementalInformationController,
                        supplementalInformationFormer);
        // when
        Throwable throwable =
                catchThrowable(() -> decoupledAuthenticationController.autoAuthenticate());

        // then
        assertThat(throwable).isInstanceOf(SessionException.class);
    }

    public TinkHttpClient mockHttpClient(RequestBuilder requestBuilder) {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        when(tinkHttpClient.request(any(URL.class))).thenReturn(requestBuilder);
        when(tinkHttpClient.request(any(String.class))).thenReturn(requestBuilder);
        return tinkHttpClient;
    }

    public RequestBuilder mockRequestBuilder() {
        RequestBuilder requestBuilder = mock(RequestBuilder.class);
        when(requestBuilder.accept(any(MediaType.class))).thenReturn(requestBuilder);
        when(requestBuilder.type(any(String.class))).thenReturn(requestBuilder);
        when(requestBuilder.header(any(), any())).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class))).thenReturn(requestBuilder);
        when(requestBuilder.body(any(Object.class), anyString())).thenReturn(requestBuilder);
        when(requestBuilder.get(ConsentDetailsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "consent_details_response.json").toFile(),
                                ConsentDetailsResponse.class));
        return requestBuilder;
    }

    private PersistentStorage createPersistentStorage() {
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(StorageKeys.CONSENT_ID, "1604575204-ba78d90");
        return persistentStorage;
    }
}
