package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class SdcTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SdcApiClient apiClient;

    public SdcTransactionalAccountFetcher(SdcApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(this::toTinkAccountWithBalance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccountWithBalance(AccountEntity accountEntity) {
        return accountEntity.toTinkAccount(
                apiClient.fetchAccountBalances(accountEntity.getResourceId()).getBalance());
    }
}
