package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.entities.LoanDetailEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanDetailsResponse extends AbstractResponse {
    @Setter private LoanDetailEntity loanDetail;
    private String loanNumber;
    private String realEstateNumber;
    private String lastUpdated;

    public LoanDetailEntity getLoanDetail() {
        return loanDetail;
    }

    public Integer calculateNumberOfMonthsBound() {
        if (loanDetail.getRemainingLoanPeriodYearly() != null
                && loanDetail.getRemainingLoanPeriodMonthly() != null) {
            return Integer.parseInt(loanDetail.getRemainingLoanPeriodYearly())
                            * DanskeBankConstants.Loan.NUMBER_OF_MONTHS_PER_YEAR
                    + Integer.parseInt(loanDetail.getRemainingLoanPeriodMonthly());
        }
        return null;
    }

    public LocalDate getNextInterestAdjustmentDate() {
        if (loanDetail.getNextInterestAdjustmentDate() != null) {
            return LocalDate.parse(
                    loanDetail.getNextInterestAdjustmentDate(),
                    DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        return null;
    }
}
