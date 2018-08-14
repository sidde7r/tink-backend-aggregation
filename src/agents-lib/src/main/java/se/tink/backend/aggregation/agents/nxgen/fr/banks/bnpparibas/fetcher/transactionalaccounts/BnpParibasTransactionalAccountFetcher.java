package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.RibListEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.UserOverviewDataEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class BnpParibasTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final BnpParibasApiClient apiClient;

    public BnpParibasTransactionalAccountFetcher(BnpParibasApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        UserOverviewDataEntity userOverview = apiClient.getUserOverview();

        if (userOverview.getContract() == null) {
            return Collections.emptyList();
        }

        return Optional.ofNullable(userOverview.getContract().getAccounts()).orElseGet(Collections::emptyList)
                .stream()
                .filter(AccountEntity::isKnownAccountType)
                .map(this::convertToTinkAccount)
                .collect(Collectors.toList());
    }

    private TransactionalAccount convertToTinkAccount(AccountEntity accountEntity) {
        RibListEntity accountDetails = apiClient.getAccountDetails(accountEntity.getIbanKey());
        return accountEntity.toTinkAccount(accountDetails);
    }
}
