package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class ImaginBankAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final ImaginBankApiClient apiClient;
    private final ImaginBankSessionStorage sessionStorage;

    public ImaginBankAccountFetcher(
            ImaginBankApiClient apiClient, ImaginBankSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> accounts = new ArrayList<>();

        boolean fetchMore = true;
        boolean fromBeginning = true;
        HolderName holderName = sessionStorage.getHolderName();

        while (fetchMore) {
            AccountsResponse accountsResponse;
            accountsResponse = apiClient.fetchAccounts(fromBeginning);
            fromBeginning = false;

            accounts.addAll(accountsResponse.getTinkAccounts(holderName));

            fetchMore = accountsResponse.isMoreData();
        }

        return accounts;
    }
}
