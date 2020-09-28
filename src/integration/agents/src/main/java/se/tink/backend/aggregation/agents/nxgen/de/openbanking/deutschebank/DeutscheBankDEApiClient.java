package se.tink.backend.aggregation.agents.nxgen.de.openbanking.deutschebank;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.DeutscheBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.entities.BankDrivenAccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.authenticator.rpc.ConsentBaseResponse;
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
    public ConsentBaseResponse getConsent(String state, String psuId) {
        ConsentBaseRequest consentBaseRequest =
                new ConsentBaseRequest(new BankDrivenAccessEntity());
        return getConsent(consentBaseRequest, state, psuId);
    }
}
