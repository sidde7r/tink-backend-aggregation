package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

public class IngAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final IngApiClient client;
    private final String currency;

    public IngAccountsFetcher(IngApiClient client, String currency) {
        this.client = client;
        this.currency = currency;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().getAccounts(currency).stream()
                .map(this::enrichAccountWithBalance)
                .collect(Collectors.toList());
    }

    private TransactionalAccount enrichAccountWithBalance(AccountEntity account) {
        final Amount balance =
                client.fetchBalances(account).getBalanceAmount(account.getCurrency());

        return account.toTinkAccount(balance);
    }
}
