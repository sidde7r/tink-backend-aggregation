package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.detail;

import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc.FetchBalancesResponse;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BalanceMapper {

    public static ExactCurrencyAmount getAvailableBalance(
            FetchBalancesResponse fetchBalancesResponse, String defaultCurrency) {
        return fetchBalancesResponse.getBalances().stream()
                .findFirst()
                .map(BalanceMapper::toTinkAmount)
                .orElse(new ExactCurrencyAmount(BigDecimal.ZERO, defaultCurrency));
    }

    public static ExactCurrencyAmount toTinkAmount(BalanceEntity balanceEntity) {
        return ExactCurrencyAmount.of(
                balanceEntity.getBalanceAmount().getAmount(),
                balanceEntity.getBalanceAmount().getCurrency());
    }
}
