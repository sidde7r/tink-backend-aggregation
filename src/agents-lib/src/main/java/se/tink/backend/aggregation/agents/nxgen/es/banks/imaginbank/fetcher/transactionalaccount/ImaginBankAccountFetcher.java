package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class ImaginBankAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final ImaginBankApiClient apiClient;
    private final SessionStorage sessionStorage;

    public ImaginBankAccountFetcher(ImaginBankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> accounts = new ArrayList<>();

        boolean fetchMore = true;
        boolean fromBeginning = true;
        while (fetchMore) {
            AccountsResponse accountsResponse;
            accountsResponse = apiClient.fetchAccounts(fromBeginning);
            fromBeginning = false;

            accounts.addAll(accountsResponse.getTinkAccounts(
                    new HolderName(sessionStorage.get(ImaginBankConstants.Storage.USER_NAME))
            ));

            fetchMore = accountsResponse.isMoreData();
        }

        return accounts;
    }
}
