package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.BalanceAmount;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.balance.BalancesItem;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountBalanceResponse {

    private List<BalancesItem> balances;

    public ExactCurrencyAmount getAvailableBalance(String defaultCurrency) {
        return balances.stream()
                .map(BalancesItem::getBalanceAmount)
                .map(BalanceAmount::getAmount)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not fetch balance"));
    }
}
