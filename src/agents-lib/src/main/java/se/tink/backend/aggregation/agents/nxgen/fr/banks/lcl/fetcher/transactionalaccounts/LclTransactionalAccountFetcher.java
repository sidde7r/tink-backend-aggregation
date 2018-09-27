package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.AccountGroupEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.GroupListEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class LclTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final LclApiClient apiClient;

    public LclTransactionalAccountFetcher(LclApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        Optional<AccountGroupEntity> checkingAccountGroup = apiClient.getCheckingAccountGroup();

        if (!checkingAccountGroup.isPresent()) {
            return Collections.emptyList();
        }

        List<AccountEntity> accounts = getAllCheckingAccounts(checkingAccountGroup.get());

        return accounts.stream()
                .map(this::getTinkAccount)
                .collect(Collectors.toList());
    }

    private List<AccountEntity> getAllCheckingAccounts(AccountGroupEntity accountGroupEntity) {
        List<GroupListEntity> groupList = accountGroupEntity.getGroupList();

        List<AccountEntity> accounts = new ArrayList<>();

        groupList.forEach(groupListEntity -> accounts.addAll(
                Optional.ofNullable(groupListEntity.getAccountList()).orElse(Collections.emptyList()))
        );

        return accounts;
    }

    private TransactionalAccount getTinkAccount(AccountEntity accountEntity) {
        Optional<AccountDetailsEntity> accountDetails = apiClient.getAccountDetails(accountEntity.getAccountNumber());

        if (!accountDetails.isPresent()) {
            return null;
        }

        return accountEntity.toTinkAccount(accountDetails.get());
    }
}
