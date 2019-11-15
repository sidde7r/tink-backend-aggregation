package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.loan;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaTypeMappers.LOAN_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.control.Option;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;

@JsonObject
public class ConsumerLoanEntity extends BaseLoanEntity {
    private AmountEntity awardedAmount;
    private AmountEntity pendingamount;
    private LoanTypeEntity loanType;
    private String digit;

    public String getDigit() {
        return digit;
    }

    @JsonIgnore
    public LoanDetails.Type getTinkLoanType() {
        return Option.of(loanType)
                .map(LoanTypeEntity::getId)
                .map(LOAN_TYPE_MAPPER::translate)
                .flatMap(Option::ofOptional)
                .getOrElse(LoanDetails.Type.OTHER);
    }

    @JsonIgnore
    private LoanAccount.Builder buildTinkLoanAccount() {
        return LoanAccount.builder(digit)
                .setExactBalance(pendingamount.toTinkAmount().negate())
                .setBankIdentifier(digit)
                .setAccountNumber(digit)
                .setName(getProduct().getDescription());
    }

    @JsonIgnore
    public LoanAccount toTinkLoanAccount() {
        final LoanDetails loanDetails =
                LoanDetails.builder(getTinkLoanType())
                        .setInitialBalance(awardedAmount.toTinkAmount())
                        .setLoanNumber(digit)
                        .setMonthlyAmortization(getNextFee().toTinkAmount())
                        .setAmortized(getRedeemedBalance().toTinkAmount())
                        .build();

        return (LoanAccount) buildTinkLoanAccount().setDetails(loanDetails).build();
    }

    @JsonIgnore
    public LoanAccount toTinkLoanAccount(double interestRate, LoanDetails loanDetails) {
        return (LoanAccount)
                buildTinkLoanAccount()
                        .setInterestRate(interestRate)
                        .setDetails(loanDetails)
                        .build();
    }
}
