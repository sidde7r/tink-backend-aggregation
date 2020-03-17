package se.tink.backend.aggregation.agents.nxgen.se.business.seb.fetcher.transactionalaccount;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebConstants.ServiceInputValues;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.SebSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.seb.rpc.Response;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final SebApiClient apiClient;
    private final SebSessionStorage sessionStorage;

    public TransactionalAccountFetcher(
            final SebApiClient apiClient, final SebSessionStorage sessionStorage) {
        this.apiClient = Objects.requireNonNull(apiClient);
        this.sessionStorage = Objects.requireNonNull(sessionStorage);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final String customerNumber = sessionStorage.getCustomerNumber();
        final Response response =
                apiClient.fetchAccounts(customerNumber, ServiceInputValues.DEFAULT_ACCOUNT_TYPE);
        final List<AccountEntity> accountEntities =
                response.getAccountEntities().orElseGet(ArrayList::new);

        return accountEntities.stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(account -> account.toTinkAccount(customerNumber))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
