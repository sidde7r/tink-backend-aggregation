package se.tink.backend.aggregation.agents.framework.testserverclient;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.framework.dao.CredentialDataDao;
import se.tink.backend.aggregation.nxgen.http.LegacyTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class AgentTestServerClient {
    private static final String PROVIDER_NAME_KEY = "providerName";
    private static final String MARKET_KEY = "market";
    private static final String CREDENTIAL_NAME_KEY = "credentialName";
    private static final String CREDENTIAL_ID_KEY = "credentialId";
    private static final String SUPPLEMENTAL_KEY_KEY = "key";
    private static final String FINANCIAL_INSTITUTION_ID_KEY = "financialInstitutionId";
    private static final String AUTOSTART_TOKEN_KEY = "autoStartToken";
    private static final String TIMEOUT_KEY = "timeout";
    private static final int TIMEOUT_MS = Math.toIntExact(TimeUnit.MINUTES.toMillis(20));

    private static AgentTestServerClient singleton;

    private final TinkHttpClient client;

    private AgentTestServerClient() {
        client = constructHttpClient();
    }

    public static AgentTestServerClient getInstance() {
        // Ought to be moved to Guice module eventually
        return singleton == null ? new AgentTestServerClient() : singleton;
    }

    private enum Urls {
        OPEN_THIRDPARTYAPP("thirdparty/open"),
        INITIATE_SUPPLEMENTAL(String.format("supplemental/{%s}", SUPPLEMENTAL_KEY_KEY)),
        WAIT_FOR_SUPPLEMENTAL(
                String.format("supplemental/{%s}/{%s}", SUPPLEMENTAL_KEY_KEY, TIMEOUT_KEY)),
        GET_PROVIDER_SESSION_CACHE(
                String.format("provider-session-cache/{%s}", FINANCIAL_INSTITUTION_ID_KEY)),
        SET_PROVIDER_SESSION_CACHE(
                String.format(
                        "provider-session-cache/{%s}/{%s}",
                        FINANCIAL_INSTITUTION_ID_KEY, TIMEOUT_KEY)),
        CREDENTIAL(String.format("credential/{%s}/{%s}", PROVIDER_NAME_KEY, CREDENTIAL_ID_KEY)),
        DUMP_DATA(
                String.format(
                        "dumpdata/{%s}/{%s}/{%s}",
                        MARKET_KEY, PROVIDER_NAME_KEY, CREDENTIAL_NAME_KEY)),
        BANKID_SEND_AUTOSTART(String.format("bankid/send/{%s}", AUTOSTART_TOKEN_KEY));

        private URL url;

        Urls(String path) {
            url = new URL(BASE_URL + path);
        }

        public URL getUrl() {
            return url;
        }

        private static final String BASE_URL = "https://127.0.0.1:7357/api/v1/";
    }

    private static TinkHttpClient constructHttpClient() {
        TinkHttpClient client = new LegacyTinkHttpClient();
        client.setTimeout(TIMEOUT_MS);

        // Disable ssl verification because of self signed certificate.
        client.disableSslVerification();
        return client;
    }

    public void openThirdPartyApp(String fields) {
        client.request(Urls.OPEN_THIRDPARTYAPP.getUrl())
                .type(MediaType.APPLICATION_JSON)
                .post(fields);
    }

    public void initiateSupplementalInformation(String key, String fields) {
        client.request(Urls.INITIATE_SUPPLEMENTAL.getUrl().parameter(SUPPLEMENTAL_KEY_KEY, key))
                .type(MediaType.APPLICATION_JSON)
                .post(fields);
    }

    public String waitForSupplementalInformation(String key, long waitFor, TimeUnit unit) {
        return client.request(
                        Urls.WAIT_FOR_SUPPLEMENTAL
                                .getUrl()
                                .parameter(SUPPLEMENTAL_KEY_KEY, key)
                                .parameter(TIMEOUT_KEY, Long.toString(unit.toSeconds(waitFor))))
                .get(String.class);
    }

    public void setProviderSessionCache(String key, String value, int expiredTimeInSeconds) {
        client.request(
                        Urls.SET_PROVIDER_SESSION_CACHE
                                .getUrl()
                                .parameter(FINANCIAL_INSTITUTION_ID_KEY, key)
                                .parameter(TIMEOUT_KEY, Integer.toString(expiredTimeInSeconds)))
                .type(MediaType.APPLICATION_JSON)
                .post(value);
    }

    public String getProviderSessionCache(String key) {
        return client.request(
                        Urls.GET_PROVIDER_SESSION_CACHE
                                .getUrl()
                                .parameter(FINANCIAL_INSTITUTION_ID_KEY, key))
                .get(String.class);
    }

    public void saveCredential(String providerName, Credentials credential) {
        client.request(
                        Urls.CREDENTIAL
                                .getUrl()
                                .parameter(PROVIDER_NAME_KEY, providerName)
                                .parameter(CREDENTIAL_ID_KEY, credential.getId()))
                .type(MediaType.APPLICATION_JSON)
                .post(credential);
    }

    public void dumpTestData(
            Provider provider, String credentialName, CredentialDataDao credentialDataDao) {
        client.request(
                        Urls.DUMP_DATA
                                .getUrl()
                                .parameter(MARKET_KEY, provider.getMarket().toLowerCase())
                                .parameter(PROVIDER_NAME_KEY, provider.getName())
                                .parameter(CREDENTIAL_NAME_KEY, credentialName))
                .type(MediaType.APPLICATION_JSON)
                .post(credentialDataDao);
    }

    public Optional<Credentials> loadCredential(String providerName, String credentialId) {
        try {
            return Optional.ofNullable(
                    client.request(
                                    Urls.CREDENTIAL
                                            .getUrl()
                                            .parameter(PROVIDER_NAME_KEY, providerName)
                                            .parameter(CREDENTIAL_ID_KEY, credentialId))
                            .get(Credentials.class));
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                return Optional.empty();
            }

            throw hre;
        }
    }

    public void sendBankIdAutoStartToken(String autoStartToken) {
        client.request(
                        Urls.BANKID_SEND_AUTOSTART
                                .getUrl()
                                .parameter(AUTOSTART_TOKEN_KEY, autoStartToken))
                .post();
    }
}
