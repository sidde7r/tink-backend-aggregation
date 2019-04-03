package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.rpc;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities.BalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FetchBalancesResponse {

    private List<BalancesEntity> balances;

    public Amount getBalanceAmount(String currency) {
        return balances != null ? getBalance(currency).getAmount() : new Amount(currency, 0);
    }

    private BalancesEntity getBalance(String currency) {
        return getByType(BalancesEntity::isExpected, currency)
            .orElse(getByType(BalancesEntity::isInterimBooked, currency)
                .orElse(getByType(BalancesEntity::isClosingBooked, currency)
                    .orElse(getFirst())));
    }

    private Optional<BalancesEntity> getByType(Predicate<BalancesEntity> predicate,
        String currency) {
        return balances.stream()
            .filter(b -> predicate.test(b) && b.getCurrency().equalsIgnoreCase(currency))
            .findFirst();
    }

    private BalancesEntity getFirst() {
        return balances.stream().findFirst().get();
    }
}
