package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.ListAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.rpc.UserDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class LaCaixaAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final LaCaixaApiClient apiClient;

    public LaCaixaAccountFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        UserDataResponse userDataResponse = apiClient.fetchUserData();
        ListAccountsResponse accountResponse = apiClient.fetchAccountList();

        if(accountResponse == null || !accountResponse.hasAccounts()) {
            return Collections.emptyList();
        }

        return accountResponse.getTransactionalAccounts(userDataResponse.getHolderName());
    }
}
