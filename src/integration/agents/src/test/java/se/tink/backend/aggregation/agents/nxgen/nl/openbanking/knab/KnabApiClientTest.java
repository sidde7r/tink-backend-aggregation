package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.libraries.serialization.utils.SerializationUtils.deserializeFromString;

import java.io.File;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabApiClient.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.ConsentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration.KnabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.time.KnabTimeProvider;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

@RunWith(JUnitParamsRunner.class)
public class KnabApiClientTest {

    private final KnabConfiguration configuration =
            deserializeFromString(resource("configuration.json"), KnabConfiguration.class);

    private final String redirectUri = "https://api.tink.com.fake/callback";

    @Mock private Filter call;

    @Mock private HttpResponse response;

    @InjectMocks private KnabApiClient apiClient;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        TinkHttpClient tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(),
                                LogMasker.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        tinkHttpClient.addFilter(call);

        apiClient =
                new KnabApiClient(
                        tinkHttpClient,
                        new MockRandomValueGenerator(),
                        new KnabTimeProvider(new ConstantLocalDateTimeSource()),
                        mock(KnabStorage.class),
                        "127.0.0.1");
        apiClient.applyConfiguration(configuration, redirectUri);

        when(call.handle(any())).thenReturn(response);
    }

    @Test
    public void shouldReturnAnonymousConsentApprovalUrl() {
        // given
        String scope = "some-scope";

        // and
        String state = "some-state";

        // expect
        assertThat(apiClient.anonymousConsentApprovalUrl(scope, state))
                .usingRecursiveComparison()
                .isEqualTo(
                        Urls.AUTHORIZE
                                .queryParam(QueryKeys.RESPONSE_TYPE, QueryValues.CODE)
                                .queryParam(QueryKeys.CLIENT_ID, configuration.getClientId())
                                .queryParam(QueryKeys.SCOPE, scope)
                                .queryParam(QueryKeys.STATE, state)
                                .queryParam(QueryKeys.REDIRECT_URI, redirectUri));
    }

    @Test
    public void shouldReturnApplicationAccessToken() {
        // given
        TokenResponse applicationAccessTokenResponse =
                givenResponseFromFile(
                        "application-access-token-response.json", TokenResponse.class);

        // when
        OAuth2Token applicationAccessToken = apiClient.applicationAccessToken();

        // then
        assertThat(applicationAccessToken).isEqualTo(applicationAccessTokenResponse.toTinkToken());
    }

    @Test
    public void shouldReturnAccessToken() {
        // given
        TokenResponse accessTokenResponse =
                givenResponseFromFile("access-token-response.json", TokenResponse.class);

        // when
        OAuth2Token accessToken = apiClient.accessToken("any-code", "any-state");

        // then
        assertThat(accessToken).isEqualTo(accessTokenResponse.toTinkToken());
    }

    @Test
    public void shouldReturnAccessTokenAfterRefresh() {
        // given
        TokenResponse refreshTokenResponse =
                givenResponseFromFile("refresh-access-token-response.json", TokenResponse.class);

        // when
        OAuth2Token accessToken = apiClient.refreshToken("any-refresh-token");

        // then
        assertThat(accessToken).isEqualTo(refreshTokenResponse.toTinkToken());
    }

    @Test
    public void shouldReturnConsentId() {
        // given
        ConsentResponse anonymousConsentResponse =
                givenResponseFromFile("consent-response.json", ConsentResponse.class);

        // when
        String consentId = apiClient.anonymousConsent(mock(OAuth2Token.class));

        // then
        assertThat(consentId).isEqualTo(anonymousConsentResponse.getConsentId());
    }

    @Test
    @Parameters
    public void shouldReturnWhetherConsentIsValid(String filename, boolean expected) {
        // given
        givenResponseFromFile(filename, ConsentStatusResponse.class);

        // when
        boolean isValid = apiClient.consentStatus("any-consent-id", mock(OAuth2Token.class));

        // then
        assertThat(isValid).isEqualTo(expected);
    }

    @SuppressWarnings("unused")
    private Object[] parametersForShouldReturnWhetherConsentIsValid() {
        return new Object[][] {
            {"consent-status-expired-response.json", false},
            {"consent-status-invalid-response.json", false},
            {"consent-status-valid-response.json", true}
        };
    }

    private <T> T givenResponseFromFile(String filename, Class<T> clazz) {
        T responseReadFromFile =
                deserializeFromString(resource(String.format("responses/%s", filename)), clazz);

        when(response.getBody(clazz)).thenReturn(responseReadFromFile);

        return responseReadFromFile;
    }

    private static File resource(String filename) {
        return new File(
                String.format(
                        "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/nl/openbanking/knab/resources/%s",
                        filename));
    }
}
