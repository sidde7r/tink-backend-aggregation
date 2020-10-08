package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities.BankDrivenAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.configuration.DeutscheMarketConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DeutscheBankDEApiClient extends DeutscheBankApiClient {

    DeutscheBankDEApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            String redirectUrl,
            DeutscheMarketConfiguration marketConfiguration) {
        super(client, persistentStorage, redirectUrl, marketConfiguration);
    }

    @Override
    public ConsentResponse getConsent(String state, String psuId) {
        ConsentRequest consentRequest = new ConsentRequest(new BankDrivenAccessEntity());
        return getConsent(consentRequest, state, psuId);
    }
}
