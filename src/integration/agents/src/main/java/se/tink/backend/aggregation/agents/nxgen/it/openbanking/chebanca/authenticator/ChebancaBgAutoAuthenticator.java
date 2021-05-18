package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.ErrorMessages.GET_TOKEN_FAILED;

import javax.servlet.http.HttpServletResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.detail.TokenResponseConverter;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.AutoTokenRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration.ChebancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.detail.HttpResponseChecker;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ChebancaBgAutoAuthenticator {
    private final ChebancaApiClient apiClient;
    private final ChebancaConfiguration configuration;
    private final PersistentStorage persistentStorage;

    public ChebancaBgAutoAuthenticator(
            ChebancaApiClient apiClient,
            AgentConfiguration<ChebancaConfiguration> agentConfiguration,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.persistentStorage = persistentStorage;
    }

    public void autoAuthenticate() {
        AutoTokenRequest tokenRequest =
                new AutoTokenRequest(
                        configuration.getAutoRefreshClientId(),
                        configuration.getAutoRefreshClientSecret(),
                        FormValues.CLIENT_CREDENTIALS);
        HttpResponse response = apiClient.createAutoAuthenticationToken(tokenRequest);
        HttpResponseChecker.checkIfSuccessfulResponse(
                response, HttpServletResponse.SC_OK, GET_TOKEN_FAILED);
        TokenResponse tokenResponse = response.getBody(TokenResponse.class);
        OAuth2Token oAuth2Token = TokenResponseConverter.toOAuthToken(tokenResponse);
        persistentStorage.put(StorageKeys.AUTO_OAUTH_TOKEN, oAuth2Token);
    }
}
