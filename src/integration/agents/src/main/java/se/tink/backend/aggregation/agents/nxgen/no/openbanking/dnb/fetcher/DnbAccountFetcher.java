package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity.AccountEntityResponse;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc.BalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.BerlinGroupAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class DnbAccountFetcher extends BerlinGroupAccountFetcher {

    private final DnbApiClient apiClient;

    public DnbAccountFetcher(final DnbApiClient apiClient) {
        super(apiClient);
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.fetchAccounts().getAccountEntities().stream()
                .map(this::addBalanceAndToTinkAccount)
                .collect(Collectors.toList());
    }

    private TransactionalAccount addBalanceAndToTinkAccount(
            final AccountEntityResponse accountEntityResponse) {
        final BalancesResponse balancesResponse =
                apiClient.fetchBalance(accountEntityResponse.getBban());
        return accountEntityResponse.toTinkAccount(balancesResponse);
    }
}
