package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf;

import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.rpc.FetchPfmPreferencesResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BnpPfApiClient {

    private final TinkHttpClient client;

    public BnpPfApiClient(TinkHttpClient client) {
        this.client = client;
    }

    public FetchPfmPreferencesResponse fetchPfmPreferences() {
        return client.request(BnpPfConstants.Url.PFMP_PREFERENCES.get()).get(FetchPfmPreferencesResponse.class);
    }

    public FetchTransactionsResponse fetchTransactionsFor(String accountId, String link) {
        String url = getTransactionsUrl(accountId, link);

        return client.request(url).get(FetchTransactionsResponse.class);
    }

    public String getTransactionsUrl(String accountId, String link) {
        if (link != null) {
            return BnpPfConstants.Url.HOST + link;
        } else {
            return BnpPfConstants.Url.TRANSACTIONS
                    .parameter("accountId", accountId)
                    .parameter("currency", BnpPfConstants.CURRENCY).get();
        }
    }
}
