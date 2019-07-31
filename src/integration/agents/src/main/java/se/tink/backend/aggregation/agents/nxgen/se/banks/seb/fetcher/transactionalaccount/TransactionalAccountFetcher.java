package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.ServiceInputValues;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SEBApiClient apiClient;
    private final SEBSessionStorage sessionStorage;

    public TransactionalAccountFetcher(SEBApiClient apiClient, SEBSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final String customerNumber = sessionStorage.getCustomerNumber();
        final Response response =
                apiClient.fetchAccounts(customerNumber, ServiceInputValues.DEFAULT_ACCOUNT_TYPE);
        final List<AccountEntity> accountEntities = response.getAccountEntities();

        return accountEntities.stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(account -> account.toTinkAccount(customerNumber))
                .collect(Collectors.toList());
    }
}
