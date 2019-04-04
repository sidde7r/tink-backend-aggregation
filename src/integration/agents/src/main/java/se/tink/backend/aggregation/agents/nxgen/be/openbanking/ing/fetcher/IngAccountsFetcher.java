package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher;

import static com.google.common.base.Predicates.or;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.BalancesEntity;
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
                client.fetchBalances(account).getBalances().stream()
                        .filter(b -> b.getCurrency().equalsIgnoreCase(currency))
                        .filter(
                                or(
                                        BalancesEntity::isExpected,
                                        BalancesEntity::isInterimBooked,
                                        BalancesEntity::isClosingBooked))
                        .map(BalancesEntity::getAmount)
                        .findFirst()
                        .orElse(new Amount(currency, 0));

        return account.toTinkAccount(balance);
    }
}
