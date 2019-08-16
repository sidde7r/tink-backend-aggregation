package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity.AccountEntityResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DnbAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final DnbApiClient apiClient;

    public DnbAccountFetcher(final DnbApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccountEntities().stream()
                .map(this::addBalanceAndToTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> addBalanceAndToTinkAccount(
            final AccountEntityResponse accountEntityResponse) {
        final BalancesResponse balancesResponse =
                apiClient.fetchBalance(accountEntityResponse.getBban());
        return accountEntityResponse.toTinkAccount(balancesResponse);
    }
}
