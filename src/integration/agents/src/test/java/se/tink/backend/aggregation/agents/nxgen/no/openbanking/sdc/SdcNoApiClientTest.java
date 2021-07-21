package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sdc;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.SdcUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.configuration.SdcConfiguration;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RunWith(JUnitParamsRunner.class)
public class SdcNoApiClientTest {

    private String MOCK_CLIENT_ID = UUID.randomUUID().toString();
    private String MOCK_STATE = UUID.randomUUID().toString();
    private URL EXPECTED_URL_NO_LOGIN =
            URL.of(
                    "https://auth.sdc.no/Account/Login?scope=psd2.aisp&response_type=code&redirect_uri=https%3A%2F%2Fapi.tink.com%2Fapi%2Fv1%2Fcredentials%2Fthird-party%2Fcallback&client_id="
                            + MOCK_CLIENT_ID
                            + "&state="
                            + MOCK_STATE
                            + "&login_type=Norwegian+Bankid+(brikke)");

    private PersistentStorage persistentStorage;

    private SdcNoApiClient apiClient;
    private SdcUrlProvider sdcUrlProvider = mock(SdcUrlProvider.class);
    private SdcConfiguration sdcConfiguration = mock(SdcConfiguration.class);

    @Before
    public void setup() {
        TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder().build(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();

        persistentStorage = mock(PersistentStorage.class);

        apiClient =
                new SdcNoApiClient(
                        httpClient,
                        sdcUrlProvider,
                        persistentStorage,
                        sdcConfiguration,
                        "https://api.tink.com/api/v1/credentials/third-party/callback");
    }

    @Test
    public void authorize_Url_returns_login_type() {
        // given
        URL authUrl = URL.of("https://auth.sdc.no/Account/Login");

        // when
        when(sdcUrlProvider.getAuthorizationUrl()).thenReturn(authUrl);
        when(sdcConfiguration.getClientId()).thenReturn(MOCK_CLIENT_ID);

        // then
        URL url = apiClient.buildAuthorizeUrl(MOCK_STATE);
        assertEquals(EXPECTED_URL_NO_LOGIN, url);
    }
}
