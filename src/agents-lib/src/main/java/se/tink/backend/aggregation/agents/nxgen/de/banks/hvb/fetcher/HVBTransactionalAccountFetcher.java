package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.rpc.AdvisorsList;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBStorage;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.WLConfig;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.fetcher.WLFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;

public final class HVBTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {
    private final WLFetcher wlFetcher;

    public HVBTransactionalAccountFetcher(final WLApiClient apiClient, final HVBStorage storage,
            final WLConfig wlConfig) {
        wlFetcher = new WLFetcher(apiClient, storage, wlConfig);
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final AccountResponse accountResponse = wlFetcher.getAccounts(AccountResponse.class);
        final AdvisorsList advisorsResponse = wlFetcher.getAccountHolders(AdvisorsList.class);
        final HolderName holderName = new HolderName(advisorsResponse.getAccountOwner());
        final Collection<TransactionalAccount> accounts = accountResponse.getTransactionalAccounts().stream()
                .map(b -> b.setHolderName(holderName))
                .map(TransactionalAccount.Builder::build)
                .collect(Collectors.toSet());
        return accounts;
    }
}
