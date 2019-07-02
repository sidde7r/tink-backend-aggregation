package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.UbiConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UbiApiClient extends CbiGlobeApiClient {

    public UbiApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        super(client, persistentStorage);
    }

    public ConsentResponse updateConsent(String consentId, UpdateConsentRequest body) {
        return createRequestInSession(Urls.CONSENTS.concat("/" + consentId))
                .header(HeaderKeys.OPERATION_NAME, HeaderValues.UPDATE_PSU_DATA)
                .put(ConsentResponse.class, body);
    }
}
