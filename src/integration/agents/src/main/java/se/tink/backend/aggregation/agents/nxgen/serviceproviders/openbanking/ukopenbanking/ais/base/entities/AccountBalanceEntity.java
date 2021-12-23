package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities;

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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkObInstantDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.UkObBalanceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@Slf4j
@JsonObject
@Data
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountBalanceEntity {
    private String accountId;

    private AmountEntity amount;

    private UkOpenBankingApiDefinitions.CreditDebitIndicator creditDebitIndicator;

    private UkObBalanceType type;

    @JsonDeserialize(using = UkObInstantDeserializer.class)
    private Instant dateTime;

    private List<CreditLineEntity> creditLine;

    public String printTypeWithCreditLines() {
        return StringUtils.join(
                "\n\t{",
                "\n\t\t",
                "balance type:",
                Optional.ofNullable(type).map(Enum::name).orElse(StringUtils.EMPTY),
                "\n\t\t",
                "credit lines:",
                CollectionUtils.emptyIfNull(creditLine).stream()
                        .map(line -> StringUtils.join(line.toString(), "\n\t\t\t"))
                        .collect(Collectors.toList()),
                "\n\t",
                "}");
    }

    public ExactCurrencyAmount getAmount() {
        ExactCurrencyAmount unsignedAmount =
                ExactCurrencyAmount.of(amount.getUnsignedAmount(), amount.getCurrency());

        return UkOpenBankingApiDefinitions.CreditDebitIndicator.CREDIT.equals(creditDebitIndicator)
                ? unsignedAmount
                : unsignedAmount.negate();
    }

    @JsonIgnore
    public ExactCurrencyAmount getAmountWithoutCreditLine() {
        ExactCurrencyAmount balanceAmount = getAmount();
        List<CreditLineEntity> includedCreditLines = getIncludedCreditLines();

        Optional<CreditLineEntity> optionalLine =
                new PrioritizedValueExtractor()
                        .pickByValuePriority(
                                includedCreditLines,
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
                .contains(type)) {
            return subtractCreditLine(line);
        }

        if (EnumSet.of(INTERIM_AVAILABLE, FORWARD_AVAILABLE, CLOSING_AVAILABLE, OPENING_AVAILABLE)
                .contains(type)) {
            return subtractCreditLineFromAvailableBalance(line);
        }

        return balanceAmount;
    }

    public List<CreditLineEntity> getIncludedCreditLines() {
        return CollectionUtils.emptyIfNull(creditLine).stream()
                .filter(CreditLineEntity::isIncluded)
                .collect(Collectors.toList());
    }

    private ExactCurrencyAmount subtractCreditLine(CreditLineEntity line) {
        ExactCurrencyAmount balanceAmount = getAmount();
        Optional<ExactCurrencyAmount> optionalCreditLineAmount =
                Optional.ofNullable(line.getAmount())
                        .map(
                                lineAmount ->
                                        ExactCurrencyAmount.of(
                                                lineAmount.getUnsignedAmount(),
                                                lineAmount.getCurrency()));

        if (!optionalCreditLineAmount.isPresent()) {
            log.info(
                    "[CREDIT LINE SUBTRACTION] No credit line amount for balance {}. Subtracting credit line skipped",
                    type);
            return balanceAmount;
        }

        ExactCurrencyAmount creditLineAmount = optionalCreditLineAmount.get();
        ExactCurrencyAmount balanceAmountWithoutCreditLine =
                balanceAmount.subtract(creditLineAmount);
        log.info(
                "[CREDIT LINE SUBTRACTION] Subtracting credit line from balance: {} - {} = {}",
                balanceAmount.getDoubleValue(),
                creditLineAmount.getDoubleValue(),
                balanceAmountWithoutCreditLine.getDoubleValue());
        // Dry run
        //         return balanceAmountWithoutCreditLine;
        return balanceAmount;
    }

    private ExactCurrencyAmount subtractCreditLineFromAvailableBalance(CreditLineEntity line) {
        ExactCurrencyAmount balanceAmount = getAmount();
        Optional<ExactCurrencyAmount> optionalCreditLineAmount =
                Optional.ofNullable(line.getAmount())
                        .map(
                                lineAmount ->
                                        ExactCurrencyAmount.of(
                                                lineAmount.getUnsignedAmount(),
                                                lineAmount.getCurrency()));

        if (!optionalCreditLineAmount.isPresent()) {
            log.info(
                    "[CREDIT LINE SUBTRACTION] No credit line amount for balance {}. Subtracting credit line skipped",
                    type);
            return balanceAmount;
        }

        ExactCurrencyAmount creditLineAmount = optionalCreditLineAmount.get();
        ExactCurrencyAmount balanceAmountWithoutCreditLine =
                balanceAmount.subtract(creditLineAmount);

        if (!line.isAvailableType()) {
            log.info(
                    "[CREDIT LINE SUBTRACTION] Subtracting credit line from available balance: {} - {} = {}",
                    balanceAmount.getDoubleValue(),
                    creditLineAmount.getDoubleValue(),
                    balanceAmountWithoutCreditLine.getDoubleValue());
            // Dry run
            //             return balanceAmountWithoutCreditLine;
            return balanceAmount;
        }

        if (balanceAmountWithoutCreditLine.getDoubleValue() < 0) {
            log.warn(
                    "[CREDIT LINE SUBTRACTION] Credit line of available type is smaller than available balance. "
                            + "This should not be possible. Setting available balance as zero");
            // Dry run
            //             return ExactCurrencyAmount.zero(balanceAmount.getCurrencyCode());
            return balanceAmount;
        }

        log.info(
                "[CREDIT LINE SUBTRACTION] Subtracting credit line from available balance: {} - {} = {}",
                balanceAmount.getDoubleValue(),
                creditLineAmount.getDoubleValue(),
                balanceAmountWithoutCreditLine.getDoubleValue());

        // Dry run
        //         return balanceAmountWithoutCreditLine;
        return balanceAmount;
    }
}
