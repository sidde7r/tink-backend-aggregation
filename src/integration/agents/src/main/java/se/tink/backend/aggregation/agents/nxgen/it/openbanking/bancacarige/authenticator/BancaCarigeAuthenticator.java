package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancacarige.authenticator;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiGlobeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BancaCarigeAuthenticator extends CbiGlobeAuthenticator {

    public BancaCarigeAuthenticator(
            CbiGlobeApiClient apiClient,
            PersistentStorage persistentStorage,
            CbiGlobeConfiguration configuration) {
        super(apiClient, persistentStorage, configuration);
    }

    @Override
    protected String createRedirectUrl(String state, ConsentType consentType) {
        // '?' and '&' need to be encoded
        return getConfiguration().getRedirectUrl()
                + CbiGlobeUtils.encodeValue("?state=" + state + "&code=" + consentType.getCode());
    }
}
