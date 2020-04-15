package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.rpc.FetchAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NordeaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final NordeaSEApiClient apiClient;

    public NordeaTransactionalAccountFetcher(NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final FetchAccountsResponse accountsResponse = apiClient.fetchAccount();

        return Optional.ofNullable(accountsResponse).orElse(new FetchAccountsResponse())
                .getAccountsList().stream()
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccount(AccountEntity account) {
        final String accountId = account.getProductId().getId();

        return apiClient.fetchAccountDetails(accountId).toTinkAccount(account);
    }
}
