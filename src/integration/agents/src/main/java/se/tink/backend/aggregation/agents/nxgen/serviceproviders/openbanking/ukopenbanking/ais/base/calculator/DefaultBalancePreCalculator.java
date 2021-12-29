package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.calculator;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalLimitType.CREDIT_LINE_PREFERRED_LIMIT_TYPES;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.CLOSING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.CLOSING_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.CLOSING_CLEARED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.EXPECTED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.FORWARD_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.INTERIM_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.INTERIM_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.INTERIM_CLEARED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.OPENING_AVAILABLE;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.OPENING_BOOKED;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType.OPENING_CLEARED;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.CreditLineEntity;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
public class DefaultBalancePreCalculator implements BalancePreCalculator {

    public ExactCurrencyAmount calculateBalanceAmountConsideringCreditLines(
            UkObBalanceType balanceType,
            ExactCurrencyAmount balanceAmount,
            List<CreditLineEntity> creditLines) {

        Optional<CreditLineEntity> optionalLine =
                new PrioritizedValueExtractor()
                        .pickByValuePriority(
                                getIncludedCreditLines(creditLines),
                                CreditLineEntity::getType,
                                CREDIT_LINE_PREFERRED_LIMIT_TYPES);

        if (!optionalLine.isPresent()) {
            return balanceAmount;
        }

        CreditLineEntity line = optionalLine.get();
        if (EnumSet.of(
                        INTERIM_BOOKED,
                        INTERIM_CLEARED,
                        EXPECTED,
                        CLOSING_BOOKED,
                        OPENING_BOOKED,
                        CLOSING_CLEARED,
                        OPENING_CLEARED)
                .contains(balanceType)) {
            return subtractCreditLine(balanceType, balanceAmount, line);
        }

        if (EnumSet.of(INTERIM_AVAILABLE, FORWARD_AVAILABLE, CLOSING_AVAILABLE, OPENING_AVAILABLE)
                .contains(balanceType)) {
            return subtractCreditLineFromAvailableBalance(balanceType, balanceAmount, line);
        }

        return balanceAmount;
    }

    private ExactCurrencyAmount subtractCreditLine(
            UkObBalanceType balanceType, ExactCurrencyAmount balanceAmount, CreditLineEntity line) {
        Optional<ExactCurrencyAmount> optionalCreditLineAmount =
                Optional.ofNullable(line.getAmount())
                        .map(
                                lineAmount ->
                                        ExactCurrencyAmount.of(
                                                lineAmount.getUnsignedAmount(),
                                                lineAmount.getCurrency()));

        if (!optionalCreditLineAmount.isPresent()) {
            log.info(
                    "[CREDIT LINE SUBTRACTION] No credit line amount for {} balance. Subtracting credit line skipped",
                    balanceType);
            return balanceAmount;
        }

        ExactCurrencyAmount creditLineAmount = optionalCreditLineAmount.get();
        ExactCurrencyAmount balanceAmountWithoutCreditLine =
                balanceAmount.subtract(creditLineAmount);
        log.info(
                "[CREDIT LINE SUBTRACTION] Subtracting credit line from {} balance: {} - {} = {}",
                balanceType,
                balanceAmount.getDoubleValue(),
                creditLineAmount.getDoubleValue(),
                balanceAmountWithoutCreditLine.getDoubleValue());
        return balanceAmountWithoutCreditLine;
    }

    private ExactCurrencyAmount subtractCreditLineFromAvailableBalance(
            UkObBalanceType balanceType, ExactCurrencyAmount balanceAmount, CreditLineEntity line) {
        Optional<ExactCurrencyAmount> optionalCreditLineAmount =
                Optional.ofNullable(line.getAmount())
                        .map(
                                lineAmount ->
                                        ExactCurrencyAmount.of(
                                                lineAmount.getUnsignedAmount(),
                                                lineAmount.getCurrency()));

        if (!optionalCreditLineAmount.isPresent()) {
            log.info(
                    "[CREDIT LINE SUBTRACTION] No credit line amount for {} balance. Subtracting credit line skipped",
                    balanceType);
            return balanceAmount;
        }

        ExactCurrencyAmount creditLineAmount = optionalCreditLineAmount.get();
        ExactCurrencyAmount balanceAmountWithoutCreditLine =
                balanceAmount.subtract(creditLineAmount);

        if (!line.isAvailableType()) {
            log.info(
                    "[CREDIT LINE SUBTRACTION] Subtracting credit line from {} available balance: {} - {} = {}",
                    balanceType,
                    balanceAmount.getDoubleValue(),
                    creditLineAmount.getDoubleValue(),
                    balanceAmountWithoutCreditLine.getDoubleValue());
            return balanceAmountWithoutCreditLine;
        }

        if (balanceAmountWithoutCreditLine.getDoubleValue() < 0) {
            log.warn(
                    "[CREDIT LINE SUBTRACTION] Credit line of available type is smaller than available balance. "
                            + "This should not be possible. Setting available balance as zero");
            return ExactCurrencyAmount.zero(balanceAmount.getCurrencyCode());
        }

        log.info(
                "[CREDIT LINE SUBTRACTION] Subtracting credit line from {} available balance: {} - {} = {}",
                balanceType,
                balanceAmount.getDoubleValue(),
                creditLineAmount.getDoubleValue(),
                balanceAmountWithoutCreditLine.getDoubleValue());
        return balanceAmountWithoutCreditLine;
    }

    private List<CreditLineEntity> getIncludedCreditLines(List<CreditLineEntity> creditLines) {
        return CollectionUtils.emptyIfNull(creditLines).stream()
                .filter(CreditLineEntity::isIncluded)
                .collect(Collectors.toList());
    }
}
