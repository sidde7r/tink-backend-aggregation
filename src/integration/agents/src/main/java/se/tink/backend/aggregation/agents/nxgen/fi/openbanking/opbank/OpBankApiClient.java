package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationRequest;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.AuthorizationResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenForm;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.configuration.OpBankConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class OpBankApiClient {

    private final TinkHttpClient client;
    private final PersistentStorage persistentStorage;
    private OpBankConfiguration configuration;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public OpBankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        this.client = client;
        this.persistentStorage = persistentStorage;
    }

    private OpBankConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    protected void setConfiguration(OpBankConfiguration configuration) {
        this.configuration = configuration;
    }

    public TokenResponse fetchNewToken() {

        HttpResponse response =
                client.request(Urls.OAUTH_TOKEN)
                        .accept(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                        .body(
                                new TokenForm()
                                        .setClientId(this.configuration.getClientId())
                                        .setClientSecret(this.configuration.getClientSecret()),
                                MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(HttpResponse.class);

        try {
            return MAPPER.readValue(response.getBodyInputStream(), TokenResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public AuthorizationResponse createNewAuthorization(String bearerToken) {
        HttpResponse response = client.request(Urls.ACCOUNTS_AUTHORIZATION)
            .type(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .body(AuthorizationRequest.expiresInDays(60))
            .header(HeaderKeys.X_API_KEY, this.configuration.getApiKey())
            .header(HeaderKeys.X_FAPI_FINANCIAL_ID, "test")
            .header(HeaderKeys.AUTHORIZATION, "Bearer " + bearerToken)
            .post(HttpResponse.class);

        try {
            return MAPPER.readValue(response.getBodyInputStream(), AuthorizationResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String fetchSignature(String jwt) {
        return client.request("http://localhost:8080/sign")
            .header("Token", jwt)
            .get(String.class);
    }



    private RequestBuilder createRequest(URL url) {
        return client.request(url)
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON);
    }

    private RequestBuilder createRequestInSession(URL url) {
        final OAuth2Token authToken = getTokenFromStorage();

        return createRequest(url).addBearerToken(authToken);
    }

    private OAuth2Token getTokenFromStorage() {
        return persistentStorage
                .get(StorageKeys.OAUTH_TOKEN, OAuth2Token.class)
                .orElseThrow(
                        () -> new IllegalStateException(SessionError.SESSION_EXPIRED.exception()));
    }
}
