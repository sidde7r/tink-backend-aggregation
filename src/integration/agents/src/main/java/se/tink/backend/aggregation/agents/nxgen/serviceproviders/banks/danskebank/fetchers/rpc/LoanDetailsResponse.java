package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities.LoanDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateFormat;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
@Slf4j
public class LoanDetailsResponse extends AbstractResponse {

    private static final String ZERO = "0";

    @Setter private LoanDetailEntity loanDetail;
    private String loanNumber;
    private String realEstateNumber;
    private String lastUpdated;

    public LoanDetailEntity getLoanDetail() {
        return loanDetail;
    }

    public Integer calculateNumberOfMonthsBound() {
        String remainingLoanPeriodYearly =
                StringUtils.isNotBlank(loanDetail.getRemainingLoanPeriodYearly())
                        ? loanDetail.getRemainingLoanPeriodYearly()
                        : ZERO;
        String remainingLoanPeriodMonthly =
                StringUtils.isNotBlank(loanDetail.getRemainingLoanPeriodMonthly())
                        ? loanDetail.getRemainingLoanPeriodMonthly()
                        : ZERO;
        if (!remainingLoanPeriodYearly.equals(ZERO) || !remainingLoanPeriodMonthly.equals(ZERO)) {
            return Integer.parseInt(remainingLoanPeriodYearly)
                            * DanskeBankConstants.Loan.NUMBER_OF_MONTHS_PER_YEAR
                    + Integer.parseInt(remainingLoanPeriodMonthly);
        }
        return null;
    }

    public LocalDate getNextInterestAdjustmentDate() {
        if (StringUtils.isNotBlank(loanDetail.getNextInterestAdjustmentDate())) {
            try {
                return DateFormat.convertToLocalDateViaInstant(
                        ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.parse(
                                loanDetail.getNextInterestAdjustmentDate()));
            } catch (ParseException e) {
                log.warn("Failed to parse nextInterestAdjustmentDate", e);
            }
        }
        return null;
    }

    public ExactCurrencyAmount getPrincipal(String currencyCode) {
        return StringUtils.isNotBlank(loanDetail.getPrincipal())
                ? ExactCurrencyAmount.of(loanDetail.getPrincipal(), currencyCode)
                : null;
    }

    public ExactCurrencyAmount getInstalment(String currencyCode) {
        return StringUtils.isNotBlank(loanDetail.getInstalment())
                ? ExactCurrencyAmount.of(loanDetail.getInstalment(), currencyCode)
                : null;
    }

    public ExactCurrencyAmount getAmortized(String currencyCode) {
        if (StringUtils.isNotBlank(loanDetail.getPrincipal())
                && StringUtils.isNotBlank(loanDetail.getDebtAmount())) {
            try {
                return ExactCurrencyAmount.of(
                        new BigDecimal(loanDetail.getPrincipal())
                                .subtract(new BigDecimal(loanDetail.getDebtAmount())),
                        currencyCode);
            } catch (NumberFormatException e) {
                log.warn("Failed to calculate amortized amount", e);
            }
        }
        return null;
    }
}
