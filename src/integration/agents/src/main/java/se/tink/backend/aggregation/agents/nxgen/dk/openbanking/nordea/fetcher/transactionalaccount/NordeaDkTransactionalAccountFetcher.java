package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.NordeaBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.rpc.GetTransactionsResponse;

public class NordeaDkTransactionalAccountFetcher<R extends GetTransactionsResponse<?>>
        extends NordeaBaseTransactionalAccountFetcher<R> {

    public NordeaDkTransactionalAccountFetcher(
            NordeaBaseApiClient apiClient,
            Class<R> transactionResponseClass,
            String providerMarket) {
        super(apiClient, transactionResponseClass, providerMarket);
    }
}
