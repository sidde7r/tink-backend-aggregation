package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc.GetBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class LansforsakringarTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount> {

    private final LansforsakringarApiClient apiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccounts().getAccounts().stream()
                .filter(AccountEntity::isNotCreditCardAccount)
                .map(accountEntity -> accountEntity.toTinkAccount(fetchBalances(accountEntity)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private GetBalancesResponse fetchBalances(AccountEntity accountEntity) {
        return apiClient.getBalances(accountEntity.getResourceId());
    }
}
