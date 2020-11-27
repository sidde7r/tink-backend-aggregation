package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup;

import java.nio.charset.StandardCharsets;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.PersistentStorageService;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration.BerlinGroupConfiguration;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.AgentRedirectTokensAuthenticationPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectTokens;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class BerlinGroupAgentPlatformStorageApiClient<T extends BerlinGroupConfiguration>
        extends BerlinGroupApiClient<T> {

    private AgentRedirectTokensAuthenticationPersistedDataAccessorFactory
            agentRedirectTokensAuthenticationPersistedDataAccessorFactory;

    public BerlinGroupAgentPlatformStorageApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            T configuration,
            CredentialsRequest request,
            String redirectUrl,
            final String qSealc) {
        super(client, persistentStorage, configuration, request, redirectUrl, qSealc);
        agentRedirectTokensAuthenticationPersistedDataAccessorFactory =
                new AgentRedirectTokensAuthenticationPersistedDataAccessorFactory(
                        new ObjectMapperFactory().getInstance());
    }

    @Override
    public OAuth2Token getTokenFromSession(String code) {
        RedirectTokens redirectTokens =
                agentRedirectTokensAuthenticationPersistedDataAccessorFactory
                        .createAgentRedirectTokensAuthenticationPersistedData(
                                new PersistentStorageService(persistentStorage)
                                        .readFromAgentPersistentStorage())
                        .getRedirectTokens()
                        .get();
        return OAuth2Token.create(
                redirectTokens.getAccessToken().getTokenType(),
                new String(redirectTokens.getAccessToken().getBody(), StandardCharsets.UTF_8),
                new String(redirectTokens.getRefreshToken().getBody(), StandardCharsets.UTF_8),
                redirectTokens.getAccessToken().getExpiresInSeconds(),
                0L);
    }
}
