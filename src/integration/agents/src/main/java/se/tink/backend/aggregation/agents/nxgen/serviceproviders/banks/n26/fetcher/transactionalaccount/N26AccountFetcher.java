package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SavingsAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.SavingsSpaceResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class N26AccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final N26ApiClient n26ApiClient;
    private static final AggregationLogger LOGGER = new AggregationLogger(N26AccountFetcher.class);

    public N26AccountFetcher(N26ApiClient n26ApiClient) {
        this.n26ApiClient = n26ApiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Collection<TransactionalAccount> result = new ArrayList<>();
        result.add(n26ApiClient.fetchAccounts().toTransactionalAccount());
        result.addAll(n26ApiClient.fetchSavingsAccounts().toSavingsAccounts());
        result.addAll(n26ApiClient.fetchSavingsSpaceAccounts().toSavingsAccounts());
        return result;
    }

}
