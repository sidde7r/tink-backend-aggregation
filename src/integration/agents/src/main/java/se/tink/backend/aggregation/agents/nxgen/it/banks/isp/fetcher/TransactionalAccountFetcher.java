package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
@RequiredArgsConstructor
public class TransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final IspApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        AccountsResponse accountsResponse = apiClient.fetchAccounts();
        logUnknownAccounts(accountsResponse);
        List<AccountEntity> accounts =
                accountsResponse.getPayload().getAccountViews().stream()
                        .flatMap(a -> a.getAccounts().stream())
                        .collect(Collectors.toList());
        return accounts.stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(AccountEntity::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private void logUnknownAccounts(AccountsResponse accountsResponse) {
        accountsResponse.getPayload().getAccountViews().stream()
                .flatMap(accountViewEntity -> accountViewEntity.getAccounts().stream())
                .forEach(AccountEntity::logAccountDetailsIfTypeUnknown);
    }
}
