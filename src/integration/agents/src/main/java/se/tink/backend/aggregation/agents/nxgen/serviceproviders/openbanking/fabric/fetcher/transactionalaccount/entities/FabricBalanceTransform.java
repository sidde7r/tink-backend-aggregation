package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.fetcher.transactionalaccount.entities;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class FabricBalanceTransform {

    private static final List<String> bookedBalancePriority =
            Arrays.asList("closingBooked", "openingBooked", "interimAvailable", "expected");
    private static final List<String> availableBalancePriority =
            Arrays.asList("interimAvailable", "expected", "forwardAvailable");

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
            final List<BalanceEntity> balances, final List<String> balancePriority) {

        return balances.stream()
                .filter(balance -> balancePriority.contains(balance.getBalanceType()))
                .min(
                        Comparator.comparing(
                                balance -> balancePriority.indexOf(balance.getBalanceType())))
                .map(balance -> balance.getBalanceAmount().toAmount());
    }
}
