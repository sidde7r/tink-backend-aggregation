package se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ComdirectAuthenticator extends Xs2aDevelopersAuthenticator {

    public ComdirectAuthenticator(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            AgentConfiguration<Xs2aDevelopersConfiguration> agentConfiguration) {
        super(apiClient, persistentStorage, agentConfiguration);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        URL result = super.buildAuthorizeUrl(state);
        return new URL(
                result.get()
                        .replace(
                                ComdirectConstants.PSD_URI_TO_BE_REPLACED,
                                ComdirectConstants.XS2A_URI_REPLACEMENT));
    }
}
