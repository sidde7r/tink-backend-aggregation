package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaFetcherApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.mappers.AccountMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.fetcher.rpc.FetchBalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class SpardaAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final SpardaFetcherApiClient apiClient;
    private final AccountMapper accountMapper;

    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .map(this::enrichWithBalance)
                .map(accountMapper::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private AccountEntity enrichWithBalance(AccountEntity accountEntity) {
        FetchBalancesResponse fetchBalancesResponse =
                apiClient.fetchBalances(accountEntity.getLinks().getBalances().getHref());

        accountEntity.setBalances(fetchBalancesResponse.getBalances());
        return accountEntity;
    }
}
