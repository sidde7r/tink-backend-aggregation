package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.account;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.libraries.amount.ExactCurrencyAmount;

class FinecoBalanceTransform {
    private static final Map<String, Integer> bookedBalancePriority = new HashMap<>();
    private static final Map<String, Integer> availableBalancePriority = new HashMap<>();

    static {
        bookedBalancePriority.put("interimBooked", 0);
        bookedBalancePriority.put("closingBooked", 1);
        bookedBalancePriority.put("openingBooked", 2);
        bookedBalancePriority.put("interimAvailable", 3);
        bookedBalancePriority.put("expected", 4);

        availableBalancePriority.put("interimAvailable", 0);
        availableBalancePriority.put("expected", 1);
        availableBalancePriority.put("forwardAvailable", 2);
    }

    private FinecoBalanceTransform() {}

    public static BalanceModule calculate(final List<BalanceEntity> balances) {
        ExactCurrencyAmount bookedBalance =
                getBalanceUsingPriorityMap(balances, bookedBalancePriority);
        ExactCurrencyAmount availableBalance =
                getBalanceUsingPriorityMap(balances, availableBalancePriority);

        BalanceBuilderStep step = BalanceModule.builder().withBalance(bookedBalance);
        if (availableBalance != null) {
            step.setAvailableBalance(availableBalance);
        }

        return step.build();
    }

    private static ExactCurrencyAmount getBalanceUsingPriorityMap(
            final List<BalanceEntity> balances, final Map<String, Integer> balancePriorityMap) {
        return balances.stream()
                .filter(balance -> balancePriorityMap.containsKey(balance.getBalanceType()))
                .min(
                        Comparator.comparing(
                                balance -> balancePriorityMap.get(balance.getBalanceType())))
                .map(balance -> balance.getBalanceAmount().toTinkAmount())
                .orElse(null);
    }
}
