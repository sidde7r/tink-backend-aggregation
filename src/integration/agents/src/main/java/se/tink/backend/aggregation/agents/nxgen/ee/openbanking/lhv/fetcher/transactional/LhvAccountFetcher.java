package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
public class LhvAccountFetcher implements AccountFetcher<TransactionalAccount> {
    protected final LhvApiClient apiClient;
    protected final SessionStorage sessionStorage;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final AccountResponse accountResponse = apiClient.getAccounts();

        return accountResponse.getAccountList().stream()
                .map(accountEntity -> accountEntity.toTinkAccount(apiClient, sessionStorage))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
