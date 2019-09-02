package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.BancoPostaConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.UpdateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BancoPostaApiClient extends CbiGlobeApiClient {

    public BancoPostaApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, boolean requestManual) {
        super(client, persistentStorage, requestManual);
    }

    public ConsentResponse updateConsent(String consentId, UpdateConsentRequest body) {
        return createRequestInSession(Urls.CONSENTS.concat("/" + consentId))
                .header(HeaderKeys.OPERATION_NAME, HeaderValues.UPDATE_PSU_DATA)
                .put(ConsentResponse.class, body);
    }
}
