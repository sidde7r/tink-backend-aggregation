package se.tink.backend.aggregation.agents.framework;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.rpc.Credentials;

public class AgentTestServerClient {
    private static final String PROVIDER_NAME_KEY = "providerName";
    private static final String CREDENTIAL_ID_KEY = "credentialId";
    private static final String SUPPLEMENTAL_KEY_KEY = "key";
    private static final String SUPPLEMENTAL_TIMEOUT_KEY = "timeout";
    private final static int TIMEOUT_MS = Math.toIntExact(TimeUnit.MINUTES.toMillis(20));
    private static final TinkHttpClient client = constructHttpClient();

    private enum Urls {
        OPEN_THIRDPARTYAPP("thirdparty/open"),
        INITIATE_SUPPLEMENTAL(String.format("supplemental/{%s}", SUPPLEMENTAL_KEY_KEY)),
        WAIT_FOR_SUPPLEMENTAL(String.format("supplemental/{%s}/{%s}", SUPPLEMENTAL_KEY_KEY, SUPPLEMENTAL_TIMEOUT_KEY)),
        CREDENTIAL(String.format("credential/{%s}/{%s}", PROVIDER_NAME_KEY, CREDENTIAL_ID_KEY));

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
        TinkHttpClient client = new TinkHttpClient(null, null);
        client.setTimeout(TIMEOUT_MS);

        // Disable ssl verification because of self signed certificate.
        client.disableSslVerification();
        return client;
    }

    public static void openThirdPartyApp(String fields) {
        client.request(Urls.OPEN_THIRDPARTYAPP.getUrl())
                .type(MediaType.APPLICATION_JSON)
                .post(fields);
    }

    public static void initiateSupplementalInformation(String key, String fields) {
        client.request(Urls.INITIATE_SUPPLEMENTAL.getUrl().parameter(SUPPLEMENTAL_KEY_KEY, key))
                .type(MediaType.APPLICATION_JSON)
                .post(fields);
    }

    public static String waitForSupplementalInformation(String key, long waitFor, TimeUnit unit) {
        return client.request(
                Urls.WAIT_FOR_SUPPLEMENTAL.getUrl()
                        .parameter(SUPPLEMENTAL_KEY_KEY, key)
                        .parameter(SUPPLEMENTAL_TIMEOUT_KEY, Long.toString(unit.toSeconds(waitFor))))
                .get(String.class);
    }

    public static void saveCredential(String providerName, Credentials credential) {
        client.request(
                Urls.CREDENTIAL.getUrl()
                        .parameter(PROVIDER_NAME_KEY, providerName)
                        .parameter(CREDENTIAL_ID_KEY, credential.getId()))
                .type(MediaType.APPLICATION_JSON)
                .post(credential);
    }

    public static Optional<Credentials> loadCredential(String providerName, String credentialId) {
        try {
            return Optional.ofNullable(
                    client.request(
                            Urls.CREDENTIAL.getUrl()
                                    .parameter(PROVIDER_NAME_KEY, providerName)
                                    .parameter(CREDENTIAL_ID_KEY, credentialId))
                            .get(Credentials.class)
            );
        } catch(HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_NOT_FOUND) {
                return Optional.empty();
            }

            throw hre;
        }
    }
}
