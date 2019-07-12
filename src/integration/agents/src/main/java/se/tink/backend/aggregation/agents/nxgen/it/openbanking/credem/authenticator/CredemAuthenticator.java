package se.tink.backend.aggregation.agents.nxgen.it.openbanking.credem.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CredemAuthenticator extends CbiGlobeAuthenticator {

    public CredemAuthenticator(
            CbiGlobeApiClient apiClient,
            PersistentStorage persistentStorage,
            CbiGlobeConfiguration configuration) {
        super(apiClient, persistentStorage, configuration);
    }

    protected URL getScaUrl(ConsentResponse consentResponse) {
        return new URL(
                consentResponse
                        .getLinks()
                        .getAuthorizeUrl()
                        .getHref()
                        .replace("api-coll.credem.it", "88.47.184.146:2443"));
    }
}
