package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class BalanceEntity {

    private AmountEntity balanceAmount;
    @Getter private String balanceType;

    public ExactCurrencyAmount toAmount() {
        return balanceAmount.toAmount();
    }

    int getBookedBalanceMappingPriority() {
        return Arrays.stream(BookedBalanceType.values())
                .filter(
                        enumBookedBalanceType ->
                                enumBookedBalanceType.getValue().equalsIgnoreCase(balanceType))
                .findAny()
                .map(BookedBalanceType::getPriority)
                .orElse(Integer.MAX_VALUE);
    }

    int getAvailableBalanceMappingPriority() {
        return Arrays.stream(AvailableBalanceType.values())
                .filter(
                        enumAvailableBalanceType ->
                                enumAvailableBalanceType.getValue().equalsIgnoreCase(balanceType))
                .findAny()
                .map(AvailableBalanceType::getPriority)
                .orElse(Integer.MAX_VALUE);
    }
}
