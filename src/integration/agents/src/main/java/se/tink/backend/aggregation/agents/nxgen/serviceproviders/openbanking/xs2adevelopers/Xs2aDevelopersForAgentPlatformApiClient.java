package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aAuthenticationDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class Xs2aDevelopersForAgentPlatformApiClient extends Xs2aDevelopersApiClient {

    private final Xs2aAuthenticationDataAccessor authenticationDataAccessor;

    public Xs2aDevelopersForAgentPlatformApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            boolean isManual,
            String userIp,
            RandomValueGenerator randomValueGenerator,
            Xs2aAuthenticationDataAccessor xs2aAuthenticationDataAccessor) {
        super(client, persistentStorage, configuration, isManual, userIp, randomValueGenerator);
        this.authenticationDataAccessor = xs2aAuthenticationDataAccessor;
    }

    @Override
    OAuth2Token getTokenFromStorage(String key) {
        return authenticationDataAccessor.getAccessToken();
    }

    @Override
    String getConsentIdFromStorage() {
        return authenticationDataAccessor.getConsent();
    }
}
