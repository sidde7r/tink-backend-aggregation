package se.tink.backend.aggregation.agents.utils.berlingroup;

import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.CLOSING_BOOKED;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.EXPECTED;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.OPENING_BOOKED;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ComparatorUtils;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceMapper {

    private static final List<BalanceType> BOOKED_BALANCE_PREFERRED_TYPES =
            ImmutableList.of(
                    INTERIM_BOOKED,
                    OPENING_BOOKED,
                    CLOSING_BOOKED,
                    EXPECTED,
                    INTERIM_AVAILABLE,
                    CLOSING_AVAILABLE);
    private static final List<BalanceType> AVAILABLE_BALANCE_PREFERRED_TYPES =
            ImmutableList.of(INTERIM_AVAILABLE, EXPECTED, FORWARD_AVAILABLE, CLOSING_AVAILABLE);

    public static ExactCurrencyAmount getBookedBalance(List<? extends BalanceMappable> balances) {
        // Booked balance is required, so we cannot continue with no way to determine it.
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }

        // We are only interested in balances without included credit limit, to determine the true
        // booked balance on the account.
        Optional<BalanceMappable> balanceEntity =
                pickByPriority(
                        balances.stream()
                                .filter(b -> !b.isCreditLimitIncluded())
                                .collect(Collectors.toList()),
                        BOOKED_BALANCE_PREFERRED_TYPES);

        // Since we need to return something, we default to first found balance. This might mean
        // unknown type, or balance with credit limit included.
        if (!balanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
            Optional<? extends BalanceMappable> balance = balances.stream().findFirst();
            if (balance.isPresent()) {
                return balance.get().toTinkAmount();
            }
        }
        return balanceEntity.get().toTinkAmount();
    }

    public static Optional<ExactCurrencyAmount> getAvailableBalance(
            List<? extends BalanceMappable> balances) {

        // We are only interested in balances without included credit limit
        Optional<BalanceMappable> balanceEntity =
                pickByPriority(
                        balances.stream()
                                .filter(b -> !b.isCreditLimitIncluded())
                                .collect(Collectors.toList()),
                        AVAILABLE_BALANCE_PREFERRED_TYPES);
        return balanceEntity.map(BalanceMappable::toTinkAmount);
    }

    public static Optional<ExactCurrencyAmount> getCreditLimit(
            List<? extends BalanceMappable> balances) {
        // In this method we only log the unknown types, but proceed with all of the balances.
        // Credit limit should appear the same in all types of balances, so there is no one type
        // that is better than other

        Map<BalanceType, List<BalanceMappable>> balancesGroupedByType =
                balances.stream()
                        .filter(b -> b.getBalanceType().isPresent())
                        .collect(Collectors.groupingBy(b -> b.getBalanceType().get()));
        List<List<BalanceMappable>> listOfMultipleBalancesPerType =
                balancesGroupedByType.values().stream()
                        .filter(x -> x.size() > 1)
                        .collect(Collectors.toList());

        if (listOfMultipleBalancesPerType.isEmpty()) {
            log.info(
                    "Weren't able to find balance type that came with both credit limit included + not included. Cannot determine credit limit.");
            return Optional.empty();
        }

        return listOfMultipleBalancesPerType.stream()
                .filter(BalanceMapper::isProperPairForDeterminingCreditLimit)
                .findFirst()
                .map(BalanceMapper::calculateCreditLimit);
    }

    private static boolean isProperPairForDeterminingCreditLimit(
            List<BalanceMappable> balancesOfSameType) {
        if (balancesOfSameType.size() != 2) {
            log.info(
                    "Found more than two balances of type {}. Skipping trying to determine creditLimit out of them.",
                    balancesOfSameType.get(0).getBalanceType());
            return false;
        }
        // No flag in balance object == flag set to false. Based on berlin group docs.
        Boolean firstFlag = balancesOfSameType.get(0).isCreditLimitIncluded();
        Boolean secondFlag = balancesOfSameType.get(1).isCreditLimitIncluded();
        return (Boolean.TRUE.equals(firstFlag) && !Boolean.TRUE.equals(secondFlag))
                || (Boolean.TRUE.equals(secondFlag) && !Boolean.TRUE.equals(firstFlag));
    }

    private static ExactCurrencyAmount calculateCreditLimit(List<BalanceMappable> balances) {
        // This method assumes that provided balances list is proper, ie. was earlier passed through
        // `isProperPairForDeterminingCreditLimit`
        return balances.get(0).toTinkAmount().subtract(balances.get(1).toTinkAmount()).abs();
    }

    private static Optional<BalanceMappable> pickByPriority(
            List<BalanceMappable> balances, List<BalanceType> priorityList) {
        Comparator<BalanceMappable> priorityComparator =
                ComparatorUtils.transformedComparator(
                        Ordering.explicit(priorityList), input -> input.getBalanceType().get());

        // Filter out the unknown types, so the priority pick can behave as it should.
        // Comparator defined above would not be able to handle the unknown types.
        return balances.stream()
                .filter(x -> x.getBalanceType().isPresent())
                .filter(b -> priorityList.contains(b.getBalanceType().get()))
                .min(priorityComparator);
    }
}
