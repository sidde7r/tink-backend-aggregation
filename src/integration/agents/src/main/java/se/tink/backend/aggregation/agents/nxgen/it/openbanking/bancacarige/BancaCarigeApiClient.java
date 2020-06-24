package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancacarige;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls.CbiGlobeUtils;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class BancaCarigeApiClient extends CbiGlobeApiClient {

    public BancaCarigeApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            boolean requestManual,
            TemporaryStorage temporaryStorage) {
        super(
                client,
                persistentStorage,
                sessionStorage,
                requestManual,
                temporaryStorage,
                InstrumentType.ACCOUNTS);
    }

    @Override
    public String createRedirectUrl(String state, ConsentType consentType) {
        // '?' and '&' need to be encoded
        return getRedirectUrl()
                + CbiGlobeUtils.encodeValue(
                        "?"
                                + QueryKeys.STATE
                                + "="
                                + state
                                + "&"
                                + QueryKeys.CODE
                                + "="
                                + consentType.getCode());
    }
}
