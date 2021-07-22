package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc;

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
public class SdcDkApiClientTest {

    private String MOCK_CLIENT_ID = UUID.randomUUID().toString();
    private String MOCK_STATE = UUID.randomUUID().toString();
    private URL EXPECTED_URL_DK_LOGIN =
            URL.of(
                    "https://auth.sdc.dk/Account/Login?scope=psd2.aisp&response_type=code&redirect_uri=https%3A%2F%2Fapi.tink.com%2Fapi%2Fv1%2Fcredentials%2Fthird-party%2Fcallback&client_id="
                            + MOCK_CLIENT_ID
                            + "&state="
                            + MOCK_STATE
                            + "&login_type=NemID+Bank+2+factor");

    private PersistentStorage persistentStorage;

    private SdcDkApiClient apiClient;
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
                new SdcDkApiClient(
                        httpClient,
                        sdcUrlProvider,
                        persistentStorage,
                        sdcConfiguration,
                        "https://api.tink.com/api/v1/credentials/third-party/callback");
    }

    @Test
    public void authorize_Url_returns_login_type() {
        // given
        URL authUrl = URL.of("https://auth.sdc.dk/Account/Login");
        when(sdcUrlProvider.getAuthorizationUrl()).thenReturn(authUrl);
        when(sdcConfiguration.getClientId()).thenReturn(MOCK_CLIENT_ID);

        // when
        URL url = apiClient.buildAuthorizeUrl(MOCK_STATE);

        // then
        assertEquals(EXPECTED_URL_DK_LOGIN, url);
    }
}
