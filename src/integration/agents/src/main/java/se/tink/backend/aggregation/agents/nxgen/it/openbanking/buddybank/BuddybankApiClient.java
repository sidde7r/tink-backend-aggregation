package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank.authenticator.rpc.BuddybankConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BuddybankApiClient extends UnicreditBaseApiClient {

    public BuddybankApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Credentials credentials,
            boolean manualRequest) {
        super(client, persistentStorage, credentials, manualRequest);
    }

    @Override
    protected Class<? extends ConsentResponse> getConsentResponseType() {
        return BuddybankConsentResponse.class;
    }

    @Override
    protected URL getScaRedirectUrlFromConsentResponse(ConsentResponse consentResponse) {
        return new URL(getConfiguration().getBaseUrl() + consentResponse.getScaRedirect());
    }

    @Override
    protected String getTransactionsDateFrom() {
        return QueryValues.TRANSACTION_FROM_DATE;
    }
}
