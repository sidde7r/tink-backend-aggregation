package se.tink.backend.aggregation.agents.utils.berlingroup;

import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.CLOSING_BOOKED;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.EXPECTED;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.OPENING_BOOKED;
import static se.tink.backend.aggregation.agents.utils.berlingroup.BalanceType.PREVIOUSLY_CLOSED_BOOKED;

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
public class BerlinGroupBalanceMapper {

    private static final List<BalanceType> BOOKED_BALANCE_PREFERRED_TYPES =
            ImmutableList.of(
                    INTERIM_BOOKED,
                    OPENING_BOOKED,
                    CLOSING_BOOKED,
                    PREVIOUSLY_CLOSED_BOOKED,
                    CLOSING_AVAILABLE,
                    EXPECTED,
                    INTERIM_AVAILABLE);
    private static final List<BalanceType> AVAILABLE_BALANCE_PREFERRED_TYPES =
            ImmutableList.of(INTERIM_AVAILABLE, EXPECTED, FORWARD_AVAILABLE);

    public static ExactCurrencyAmount getBookedBalance(List<BalanceEntity> balances) {
        // Booked balance is required, so we cannot continue with no way to determine it.
        if (balances.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot determine booked balance from empty list of balances.");
        }
        logUnknownTypes(balances, "getBookedBalance");

        // We are only interested in balances without included credit limit, to determine the true
        // booked balance on the account.
        Optional<BalanceEntity> balanceEntity =
                pickByPriority(
                        balances.stream()
                                .filter(x -> !Boolean.TRUE.equals(x.getCreditLimitIncluded()))
                                .collect(Collectors.toList()),
                        BOOKED_BALANCE_PREFERRED_TYPES);

        // Since we need to return something, we default to first found balance. This might mean
        // unknown type, or balance with credit limit included.
        if (!balanceEntity.isPresent()) {
            log.warn(
                    "Couldn't determine booked balance of known type, and no credit limit included. Defaulting to first provided balance.");
        }
        return balanceEntity.orElse(balances.stream().findFirst().get()).toTinkAmount();
    }

    public static Optional<ExactCurrencyAmount> getAvailableBalance(List<BalanceEntity> balances) {
        logUnknownTypes(balances, "getAvailableBalance");

        // We are only interested in balances without included credit limit
        Optional<BalanceEntity> balanceEntity =
                pickByPriority(
                        balances.stream()
                                .filter(x -> !Boolean.TRUE.equals(x.getCreditLimitIncluded()))
                                .collect(Collectors.toList()),
                        AVAILABLE_BALANCE_PREFERRED_TYPES);
        return balanceEntity.map(BalanceEntity::toTinkAmount);
    }

    public static Optional<ExactCurrencyAmount> getCreditLimit(List<BalanceEntity> balances) {
        // In this method we only log the unknown types, but proceed with all of the balances.
        // Credit limit should appear the same in all types of balances, so there is no one type
        // that is better than other
        logUnknownTypes(balances, "getCreditLimit");

        Map<String, List<BalanceEntity>> balancesGroupedByType =
                balances.stream().collect(Collectors.groupingBy(BalanceEntity::getBalanceType));
        List<List<BalanceEntity>> listOfMultipleBalancesPerType =
                balancesGroupedByType.values().stream()
                        .filter(x -> x.size() > 1)
                        .collect(Collectors.toList());

        if (listOfMultipleBalancesPerType.isEmpty()) {
            log.info(
                    "Weren't able to find balance type that came with both credit limit included + not included. Cannot determine credit limit.");
            return Optional.empty();
        }

        return listOfMultipleBalancesPerType.stream()
                .filter(BerlinGroupBalanceMapper::isProperPairForDeterminingCreditLimit)
                .findFirst()
                .map(BerlinGroupBalanceMapper::calculateCreditLimit);
    }

    private static boolean isProperPairForDeterminingCreditLimit(
            List<BalanceEntity> balancesOfSameType) {
        if (balancesOfSameType.size() != 2) {
            log.info(
                    "Found more than two balances of type {}. Skipping trying to determine creditLimit out of them.",
                    balancesOfSameType.get(0).getBalanceType());
            return false;
        }
        // No flag in balance object == flag set to false. Based on berlin group docs.
        Boolean firstFlag = balancesOfSameType.get(0).getCreditLimitIncluded();
        Boolean secondFlag = balancesOfSameType.get(1).getCreditLimitIncluded();
        return (Boolean.TRUE.equals(firstFlag) && !Boolean.TRUE.equals(secondFlag))
                || (Boolean.TRUE.equals(secondFlag) && !Boolean.TRUE.equals(firstFlag));
    }

    private static ExactCurrencyAmount calculateCreditLimit(List<BalanceEntity> balances) {
        // This method assumes that provided balances list is proper, ie. was earlier passed through
        // `isProperPairForDeterminingCreditLimit`
        return balances.get(0).toTinkAmount().subtract(balances.get(1).toTinkAmount()).abs();
    }

    private static void logUnknownTypes(List<BalanceEntity> balances, String sourceOperation) {
        List<String> unknownTypes =
                balances.stream()
                        .map(BalanceEntity::getBalanceType)
                        .filter(x -> !BalanceType.isKnownType(x))
                        .collect(Collectors.toList());
        if (!unknownTypes.isEmpty()) {
            log.warn(
                    "Found balance entities with unknown balanceType during "
                            + sourceOperation
                            + ": "
                            + String.join(", ", unknownTypes));
        }
    }

    private static Optional<BalanceEntity> pickByPriority(
            List<BalanceEntity> balances, List<BalanceType> priorityList) {
        Comparator<BalanceEntity> priorityComparator =
                ComparatorUtils.transformedComparator(
                        Ordering.explicit(priorityList),
                        input -> BalanceType.findByStringType(input.getBalanceType()).get());

        // Filter out the unknown types, so the priority pick can behave as it should.
        // Comparator defined above would not be able to handle the unknown types.
        return balances.stream()
                .filter(x -> BalanceType.isKnownType(x.getBalanceType()))
                .filter(
                        b ->
                                priorityList.contains(
                                        BalanceType.findByStringType(b.getBalanceType()).get()))
                .min(priorityComparator);
    }
}
