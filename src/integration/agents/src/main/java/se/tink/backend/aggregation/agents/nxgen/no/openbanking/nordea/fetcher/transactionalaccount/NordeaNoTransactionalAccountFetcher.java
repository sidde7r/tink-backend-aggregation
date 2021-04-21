package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;

public class NordeaNoTransactionalAccountFetcher<R extends GetTransactionsResponse<?>>
        extends NordeaBaseTransactionalAccountFetcher<R> {

    public NordeaNoTransactionalAccountFetcher(
            NordeaBaseApiClient apiClient,
            Class<R> transactionResponseClass,
            String providerMarket) {
        super(apiClient, transactionResponseClass, providerMarket);
    }
}
