package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount;

import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.rpc.AccountFetchResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.rpc.BalanceFetchResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class FidorAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private FidorApiClient apiClient;

    public FidorAccountFetcher(FidorApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountFetchResponse accountResponse = apiClient.fetchAccouns();
        for (AccountEntity entity : accountResponse.getAccounts()) {
            BalanceFetchResponse balanceResponse = apiClient.fetchBalances(entity.getResourceId());
            entity.setBalances(balanceResponse.getBalances());
        }
        return accountResponse.toTinkAccounts();
    }
}
