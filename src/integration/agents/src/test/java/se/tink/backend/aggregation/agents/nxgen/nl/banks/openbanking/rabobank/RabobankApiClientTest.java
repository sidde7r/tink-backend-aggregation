package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankUrlFactory;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class RabobankApiClientTest {

    private static final String RESOURCE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank/resources/";

    @Mock private Filter callFilter;

    @Mock private HttpResponse response;

    @Mock private RabobankSignatureHeaderBuilder rabobankSignatureHeaderBuilder;

    @Mock private RabobankConfiguration rabobankConfiguration;

    private RabobankApiClient rabobankApiClient;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        given(rabobankConfiguration.getUrls())
                .willReturn(
                        new RabobankUrlFactory(
                                new URL("https://fake.url"), new URL("https://fake.url")));

        rabobankApiClient = rabobankApiClient();
    }

    @Test
    public void shouldSetCorrectlyConsentStatusWhenOneScopeIsNotPresent() {
        // given
        bankReturnsConsent("RabobankConsentResponseWithOneMissingScope.json");

        // expect
        assertThatNoException().isThrownBy(rabobankApiClient::checkConsentStatus);
    }

    @Test
    public void shouldThrowExceptionWhenAtLeastOneStatusIsNotValid() {
        // given
        bankReturnsConsent("RabobankConsentResponseWithOneInvalidStatus.json");

        // expect
        assertThatThrownBy(rabobankApiClient::checkConsentStatus)
                .isInstanceOf(SessionException.class)
                .hasMessageContaining("At least one consent status is not valid");
    }

    private void bankReturnsConsent(String consentFileName) {
        given(callFilter.handle(any())).willReturn(response);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(ConsentDetailsResponse.class))
                .willReturn(deserializeConsentFromFile(RESOURCE_PATH + consentFileName));
    }

    private RabobankApiClient rabobankApiClient() {
        return new RabobankApiClient(
                initializeTinkHttpClient(),
                initializePersistentStorage(),
                rabobankConfiguration,
                rabobankSignatureHeaderBuilder,
                new RabobankUserIpInformation(true, "127.0.0.1"));
    }

    private PersistentStorage initializePersistentStorage() {
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put(StorageKey.CONSENT_ID, "consent123");
        persistentStorage.put(
                StorageKey.OAUTH_TOKEN, OAuth2Token.create("bearer", "sss", "rrr", 123456789));
        return persistentStorage;
    }

    TinkHttpClient initializeTinkHttpClient() {
        NextGenTinkHttpClient tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(), LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        tinkHttpClient.addFilter(callFilter);
        return tinkHttpClient;
    }

    private static ConsentDetailsResponse deserializeConsentFromFile(String filePath) {
        return SerializationUtils.deserializeFromString(
                Paths.get(filePath).toFile(), ConsentDetailsResponse.class);
    }
}
