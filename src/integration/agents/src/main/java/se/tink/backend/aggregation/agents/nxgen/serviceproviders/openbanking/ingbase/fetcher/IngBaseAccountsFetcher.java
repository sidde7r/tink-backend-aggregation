package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher;

import com.google.common.base.Predicates;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.BalancesEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

public class IngBaseAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final IngBaseApiClient client;
    private final String currency;

    public IngBaseAccountsFetcher(IngBaseApiClient client, String currency) {
        this.client = client;
        this.currency = currency;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return client.fetchAccounts().getAccounts(currency).stream()
                .map(this::enrichAccountWithBalance)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> enrichAccountWithBalance(AccountEntity account) {
        Amount balance =
                client.fetchBalances(account).getBalances().stream()
                        .filter(b -> b.getCurrency().equalsIgnoreCase(currency))
                        .filter(
                                Predicates.or(
                                        BalancesEntity::isExpected,
                                        BalancesEntity::isInterimBooked,
                                        BalancesEntity::isClosingBooked))
                        .map(BalancesEntity::getAmount)
                        .findFirst()
                        .orElse(new Amount(currency, 0));

        return account.toTinkAccount(balance);
    }
}
