package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.transactionalaccount.entities.AccountsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AllArgsConstructor
public class BankDataAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final BankDataApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final AccountResponse accountResponse = apiClient.fetchAccounts();

        return ListUtils.emptyIfNull(accountResponse).stream()
                .filter(AccountsEntity::isTransactionalAccount)
                .map(AccountsEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
