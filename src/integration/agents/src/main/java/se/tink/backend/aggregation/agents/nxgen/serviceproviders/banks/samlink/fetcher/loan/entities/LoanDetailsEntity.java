package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.SamlinkConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

public class LoanDetailsEntity extends LoanEntity {
    private static final AggregationLogger log = new AggregationLogger(LoanDetailsEntity.class);

    private Boolean financingLimitProduct;

    @JsonProperty("loanNextPayment")
    private NextLoanPayment nextPayment;

    @JsonProperty("loanBasicInfo")
    private BasicLoanInfo basicInfo;

    public LoanAccount toAccount() {
        return LoanAccount.builder(getLoanNumber(), getBalance().toTinkAmount().negate())
                .setAccountNumber(getLoanNumber())
                .setInterestRate(basicInfo.getInterestRateTotal() / 100)
                .setDetails(LoanDetails.builder(getType()).setLoanNumber(getLoanNumber()).build())
                .build();
    }

    private LoanDetails.Type getType() {
        switch (getLoanPurpose().toUpperCase()) {
            case SamlinkConstants.LoanType.MORTGAGE:
                return LoanDetails.Type.MORTGAGE;
            case SamlinkConstants.LoanType.STUDENT:
                return LoanDetails.Type.STUDENT;
            case SamlinkConstants.LoanType.OTHER:
                break;
            default:
                log.info(
                        SamlinkConstants.LogTags.UNKNOWN_LOAN_TYPE.toString()
                                + " RawType: "
                                + getLoanPurpose());
        }

        return LoanDetails.Type.OTHER;
    }

    @Override
    public String getLoanNumber() {
        return Optional.ofNullable(super.getLoanNumber()).orElse(basicInfo.getLoanNumber());
    }

    @Override
    public AmountEntity getBalance() {
        return Optional.ofNullable(super.getBalance()).orElse(basicInfo.getAmount());
    }
}
