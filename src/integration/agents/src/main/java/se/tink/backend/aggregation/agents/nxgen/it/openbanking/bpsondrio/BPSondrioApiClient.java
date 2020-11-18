package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpsondrio;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class BPSondrioApiClient extends CbiGlobeApiClient {

    public BPSondrioApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            boolean requestManual,
            TemporaryStorage temporaryStorage,
            CbiGlobeProviderConfiguration providerConfiguration,
            String psuIpAddress) {
        super(
                client,
                persistentStorage,
                sessionStorage,
                temporaryStorage,
                InstrumentType.ACCOUNTS,
                providerConfiguration,
                requestManual ? psuIpAddress : null);
    }

    @Override
    public String createRedirectUrl(String state, ConsentType consentType, String authResult) {
        // '?' and '&' need to be encoded
        return redirectUrl
                + CbiGlobeUtils.getEncondedRedirectURIQueryParams(
                        state, consentType.getCode(), authResult);
    }
}
