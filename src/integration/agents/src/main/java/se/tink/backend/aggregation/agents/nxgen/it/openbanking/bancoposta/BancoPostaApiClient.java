package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc.ConsentScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.ConsentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class BancoPostaApiClient extends CbiGlobeApiClient {

    public BancoPostaApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            boolean requestManual,
            TemporaryStorage temporaryStorage) {
        super(client, persistentStorage, requestManual, temporaryStorage, InstrumentType.ACCOUNTS);
    }

    @Override
    public ConsentScaResponse createConsent(
            String state, ConsentType consentType, ConsentRequest consentRequest) {
        RequestBuilder rb = createConsentRequest(state, consentType);
        return rb.post(ConsentScaResponse.class, consentRequest);
    }
}
