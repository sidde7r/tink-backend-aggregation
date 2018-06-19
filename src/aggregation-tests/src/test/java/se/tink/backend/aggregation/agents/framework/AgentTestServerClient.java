package se.tink.backend.aggregation.agents.framework;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.rpc.Credentials;

public class AgentTestServerClient {
    private static final TinkHttpClient client = new TinkHttpClient(null, null);

    private enum Urls {
        SUPPLEMENTAL("supplemental"),
        CREDENTIAL("credential/{providerName}/{credentialId}");

        private URL url;

        Urls(String path) {
            url = new URL(BASE_URL + path);
        }

        public URL getUrl() {
            return url;
        }

        private static final String BASE_URL = "http://127.0.0.1:7357/api/v1/";
    }

    public static String askForSupplementalInformation(String fields) {
        return client.request(Urls.SUPPLEMENTAL.getUrl())
                .type(MediaType.APPLICATION_JSON)
                .post(String.class, fields);
    }

    public static void saveCredential(String providerName, Credentials credential) {
        client.request(
                Urls.CREDENTIAL.getUrl()
                        .parameter("providerName", providerName)
                        .parameter("credentialId", credential.getId()))
                .type(MediaType.APPLICATION_JSON)
                .post(credential);
    }

    public static Optional<Credentials> loadCredential(String providerName, String credentialId) {
        try {
            return Optional.ofNullable(
                    client.request(
                            Urls.CREDENTIAL.getUrl()
                                    .parameter("providerName", providerName)
                                    .parameter("credentialId", credentialId))
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
