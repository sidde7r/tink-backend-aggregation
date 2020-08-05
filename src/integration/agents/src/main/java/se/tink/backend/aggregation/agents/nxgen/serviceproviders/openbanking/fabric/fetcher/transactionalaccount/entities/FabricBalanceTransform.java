package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class FabricBalanceTransform {
    private static final Map<String, Integer> bookedBalancePriority = new HashMap<>();

    private static final Map<String, Integer> availableBalancePriority = new HashMap<>();

    static {
        bookedBalancePriority.put("closingBooked", 1);
        bookedBalancePriority.put("openingBooked", 2);
        bookedBalancePriority.put("interimAvailable", 3);
        bookedBalancePriority.put("expected", 4);

        availableBalancePriority.put("interimAvailable", 0);
        availableBalancePriority.put("expected", 1);
        availableBalancePriority.put("forwardAvailable", 2);
    }

    private FabricBalanceTransform() {}

    public static BalanceModule calculate(final List<BalanceEntity> balances) {
        Optional<ExactCurrencyAmount> bookedBalance =
                getBalanceUsingPriorityMap(balances, bookedBalancePriority);

        Optional<ExactCurrencyAmount> availableBalance =
                getBalanceUsingPriorityMap(balances, availableBalancePriority);

        BalanceBuilderStep step =
                BalanceModule.builder()
                        .withBalance(
                                bookedBalance.orElseThrow(
                                        () -> new IllegalStateException("Balance is missing.")));
        availableBalance.ifPresent(step::setAvailableBalance);

        return step.build();
    }

    private static Optional<ExactCurrencyAmount> getBalanceUsingPriorityMap(
            final List<BalanceEntity> balances, final Map<String, Integer> balancePriorityMap) {
        return balances.stream()
                .filter(balance -> balancePriorityMap.containsKey(balance.getBalanceType()))
                .min(
                        Comparator.comparing(
                                balance -> balancePriorityMap.get(balance.getBalanceType())))
                .map(balance -> balance.getBalanceAmount().toAmount());
    }
}
