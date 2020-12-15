package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities.LoanDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Slf4j
public class LoanDetailsResponse extends AbstractResponse {

    private static final String ZERO = "0";
    private static final String DATE_PATTERN = "yyyyMMdd";

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
                return LocalDate.parse(
                        loanDetail.getNextInterestAdjustmentDate(),
                        DateTimeFormatter.ofPattern(DATE_PATTERN));
            } catch (DateTimeParseException e) {
                log.warn(
                        "Failed to parse nextInterestAdjustmentDate: {}",
                        loanDetail.getNextInterestAdjustmentDate());
            }
        }
        return null;
    }

    public ExactCurrencyAmount getPrincipal(String currencyCode) {
        return StringUtils.isNotBlank(loanDetail.getPrincipal())
                ? ExactCurrencyAmount.of(loanDetail.getPrincipal(), currencyCode)
                : null;
    }
}
