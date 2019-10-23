package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc;

import java.math.BigDecimal;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.AccountIbanEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class FetchBalancesResponse {
    private AccountIbanEntity account;
    private Collection<BalanceEntity> balances;

    public ExactCurrencyAmount getAvailableBalance(String defaultCurrency) {
        return balances.stream()
                .map(this::toTinkAmount)
                .findFirst()
                .orElse(new ExactCurrencyAmount(BigDecimal.ZERO, defaultCurrency));
    }

    private ExactCurrencyAmount toTinkAmount(BalanceEntity balanceEntity) {
        return new ExactCurrencyAmount(
                BigDecimal.valueOf(
                        Double.parseDouble(balanceEntity.getBalanceAmount().getAmount())),
                balanceEntity.getBalanceAmount().getCurrency());
    }
}
