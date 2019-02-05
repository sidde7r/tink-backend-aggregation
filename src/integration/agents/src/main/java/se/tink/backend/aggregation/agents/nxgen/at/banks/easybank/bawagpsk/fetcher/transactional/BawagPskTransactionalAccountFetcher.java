package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.transactional;

import java.util.Collection;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.BawagPskApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.fetcher.BawagPskAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.GetAccountInformationListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.libraries.pair.Pair;

public final class BawagPskTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {

    private final BawagPskAccountFetcher accountFetcher;

    public BawagPskTransactionalAccountFetcher(final BawagPskApiClient bawagPskApiClient) {
        accountFetcher = new BawagPskAccountFetcher(bawagPskApiClient);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final Pair<GetAccountInformationListResponse, Map<String, String>> pair =
                accountFetcher.fetchAccountData();

        return pair.first.extractTransactionalAccounts(pair.second);
    }
}
