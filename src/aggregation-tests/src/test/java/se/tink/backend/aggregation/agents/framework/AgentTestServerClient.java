package se.tink.backend.aggregation.agents.framework;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.rpc.Credentials;

public class AgentTestServerClient {
    private static final String PROVIDER_NAME_KEY = "providerName";
    private static final String CREDENTIAL_ID_KEY = "credentialId";
    private final static int TIMEOUT_MS = 2 * 60 * 1000; // 2 minutes
    private static final TinkHttpClient client = constructHttpClient();

    private enum Urls {
        SUPPLEMENTAL("supplemental"),
        CREDENTIAL(String.format("credential/{%s}/{%s}", PROVIDER_NAME_KEY, CREDENTIAL_ID_KEY));

        private URL url;

        Urls(String path) {
            url = new URL(BASE_URL + path);
        }

        public URL getUrl() {
            return url;
        }

        private static final String BASE_URL = "http://127.0.0.1:7357/api/v1/";
    }

    private static TinkHttpClient constructHttpClient() {
        TinkHttpClient client = new TinkHttpClient(null, null);
        client.setTimeout(TIMEOUT_MS);
        return client;
    }

    public static String askForSupplementalInformation(String fields) {
        return client.request(Urls.SUPPLEMENTAL.getUrl())
                .type(MediaType.APPLICATION_JSON)
                .post(String.class, fields);
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
