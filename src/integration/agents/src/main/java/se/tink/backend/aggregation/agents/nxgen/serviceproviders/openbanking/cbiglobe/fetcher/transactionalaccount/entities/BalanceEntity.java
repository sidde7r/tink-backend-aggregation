package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.entities;

import java.util.Arrays;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {

    public static Amount defaultAmount = Amount.inEUR(0);
    private AmountEntity balanceAmount;
    private String balanceType;

    public boolean isAvailable() {
        return balanceType.equalsIgnoreCase("interimAvailable")
                || balanceType.equalsIgnoreCase("forwardAvailable");
    }

    public Amount toAmount() {
        return balanceAmount.toAmount();
    }

    public boolean isExpected() {
        return balanceType.equalsIgnoreCase("expected");
    }

    public int getBalanceMappingPriority() {
        return Arrays.stream(BalanceType.values())
                .filter(enumBalanceType -> enumBalanceType.getValue().equalsIgnoreCase(balanceType))
                .findAny()
                .map(BalanceType::getPriority)
                .orElse(Integer.MAX_VALUE);
    }
}
