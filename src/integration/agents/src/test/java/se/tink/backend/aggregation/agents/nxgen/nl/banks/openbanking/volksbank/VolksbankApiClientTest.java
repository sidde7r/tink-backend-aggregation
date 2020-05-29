package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.ArgumentManager;
import se.tink.backend.aggregation.agents.framework.ArgumentManager.ArgumentManagerEnum;
import se.tink.backend.aggregation.agents.framework.context.NewAgentTestContext;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Paths;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.TokenParams;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.fakelogmasker.FakeLogMasker;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl.LoggingMode;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public final class VolksbankApiClientTest {

    private enum Arg implements ArgumentManagerEnum {
        CLIENT_ID, // lfffffffffffffffffffffffffffffffff
        CLIENT_SECRET, // cccccccccccccccccccccccccccccccccc
        REFRESH_TOKEN, // ffffffff-ffff-ffff-ffff-ffffffffffff
        CONSENT_ID; // SNS7777777777777

        private final boolean optional;

        Arg(boolean optional) {
            this.optional = optional;
        }

        Arg() {
            this.optional = false;
        }

        @Override
        public boolean isOptional() {
            return optional;
        }
    }

    private final ArgumentManager<Arg> manager = new ArgumentManager<>(Arg.values());

    @Before
    public void before() {
        manager.before();
    }

    @AfterClass
    public static void afterClass() {
        ArgumentManager.afterClass();
    }

    @Test
    public void testRefreshToken() {
        TinkHttpClient tinkHttpClient = createHttpClient();

        VolksbankUrlFactory urlFactory = new VolksbankUrlFactory(Urls.HOST, "snsbank");

        VolksbankApiClient apiClient = new VolksbankApiClient(tinkHttpClient, urlFactory);

        URL url =
                urlFactory
                        .buildURL(Paths.TOKEN)
                        .queryParam(QueryParams.GRANT_TYPE, TokenParams.REFRESH_TOKEN)
                        .queryParam(QueryParams.REFRESH_TOKEN, manager.get(Arg.REFRESH_TOKEN));

        apiClient.getBearerToken(url, manager.get(Arg.CLIENT_ID), manager.get(Arg.CLIENT_SECRET));
    }

    @Test
    public void testConsentStatus() {
        TinkHttpClient tinkHttpClient = createHttpClient();

        VolksbankUrlFactory urlFactory = new VolksbankUrlFactory(Urls.HOST, "snsbank");

        VolksbankApiClient apiClient = new VolksbankApiClient(tinkHttpClient, urlFactory);

        String consentId = manager.get(Arg.CONSENT_ID);

        apiClient.consentStatusRequest(manager.get(Arg.CLIENT_ID), consentId);
    }

    private static TinkHttpClient createHttpClient() {
        TinkHttpClient tinkHttpClient =
                NextGenTinkHttpClient.builder(
                                new FakeLogMasker(), LoggingMode.LOGGING_MASKER_COVERS_SECRETS)
                        .build();
        EidasProxyConfiguration proxyConfiguration =
                EidasProxyConfiguration.createLocal(
                        "https://eidas-proxy.staging.aggregation.tink.network");
        tinkHttpClient.setEidasIdentity(
                new EidasIdentity(
                        NewAgentTestContext.TEST_CLUSTERID,
                        NewAgentTestContext.TEST_APPID,
                        VolksbankAgent.class));
        tinkHttpClient.setDebugOutput(true);
        return tinkHttpClient;
    }

    @Test
    public void test() {
        TinkHttpClient tinkHttpClient = createHttpClient();

        VolksbankUrlFactory urlFactory = new VolksbankUrlFactory(Urls.HOST, "snsbank");

        VolksbankApiClient apiClient = new VolksbankApiClient(tinkHttpClient, urlFactory);

        URL url =
                new URL(
                        "https://main.staging.oxford.tink.se/api/v1/credentials/third-party/callback");

        ConsentResponse response1 = apiClient.consentRequest(url, manager.get(Arg.CLIENT_ID));
    }
}
