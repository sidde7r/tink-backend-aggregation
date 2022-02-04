package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.agent.runtime.operation.UserImpl;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc.ConsentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration.RabobankConfiguration;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker.LoggingMode;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(MockitoJUnitRunner.class)
public class RabobankConsentStatusValidatorTest {

    private static final String OAUTH_2_TOKEN_STORAGE_KEY = "oauth2_access_token";
    private static final String CONSENT_ID_STORAGE_KEY = "consentId";

    @Mock private Filter callFilter;

    @Mock private HttpResponse response;

    @Mock private RabobankSignatureHeaderBuilder rabobankSignatureHeaderBuilder;

    private RabobankConsentStatusValidator rabobankConsentStatusValidator;
    private PersistentStorage persistentStorage;

    @Before
    public void setup() {
        persistentStorage = initializePersistentStorage();
        rabobankConsentStatusValidator =
                new RabobankConsentStatusValidator(
                        rabobankApiClient(persistentStorage),
                        persistentStorage,
                        rabobankSignatureHeaderBuilder,
                        new RabobankConfiguration());
    }

    @Test
    public void shouldSetCorrectlyConsentStatusWhenOneScopeIsNotPresent() {
        // given
        bankReturnsConsent("RabobankConsentResponseWithOneMissingScope.json");

        // expect
        assertThatNoException().isThrownBy(rabobankConsentStatusValidator::validateConsentStatus);
    }

    @Test
    public void shouldThrowExceptionWhenAtLeastOneStatusIsNotValid() {
        // given
        bankReturnsConsent("RabobankConsentResponseWithOneInvalidStatus.json");

        // expect
        assertThatThrownBy(rabobankConsentStatusValidator::validateConsentStatus)
                .isInstanceOf(SessionException.class)
                .hasMessageContaining("At least one consent status is not valid");

        // and
        storageIsCleared();
    }

    private void storageIsCleared() {
        assertThat(persistentStorage.containsKey(OAUTH_2_TOKEN_STORAGE_KEY)).isFalse();
        assertThat(persistentStorage.containsKey(CONSENT_ID_STORAGE_KEY)).isFalse();
    }

    private void bankReturnsConsent(String consentFileName) {
        given(callFilter.handle(any())).willReturn(response);
        given(response.getStatus()).willReturn(200);
        given(response.getBody(ConsentDetailsResponse.class))
                .willReturn(deserializeConsentFromFile(consentFileName));
    }

    private RabobankApiClient rabobankApiClient(PersistentStorage persistentStorage) {
        return new RabobankApiClient(
                initializeTinkHttpClient(),
                persistentStorage,
                new RabobankConfiguration(),
                rabobankSignatureHeaderBuilder,
                new UserImpl(true, true, "127.0.0.1", null));
    }

    private PersistentStorage initializePersistentStorage() {
        PersistentStorage persistentStorage = new PersistentStorage();
        persistentStorage.put("consentId", "consent123");
        persistentStorage.put(
                "oauth2_access_token", OAuth2Token.create("bearer", "sss", "rrr", 123456789));
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
        String resourcePath =
                "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/banks/openbanking/rabobank/resources/";
        return SerializationUtils.deserializeFromString(
                Paths.get(resourcePath + filePath).toFile(), ConsentDetailsResponse.class);
    }
}
