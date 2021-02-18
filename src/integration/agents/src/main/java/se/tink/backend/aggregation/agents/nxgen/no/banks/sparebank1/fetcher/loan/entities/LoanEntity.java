package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
@Data
public class LoanEntity {
    private String id;
    private String name;
    private String formattedNumber;
    private String type;

    @JsonIgnore
    public LoanAccount toTinkLoan(LoanDetailsEntity loanDetails) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(buildLoanDetails(loanDetails))
                .withId(buildIdModule())
                .build();
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(formattedNumber)
                .withAccountNumber(formattedNumber)
                .withAccountName(name)
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.NO,
                                StringUtils.removeNonAlphaNumeric(formattedNumber)))
                .build();
    }

    private LoanModule buildLoanDetails(LoanDetailsEntity loanDetails) {
        return LoanModule.builder()
                .withType(getLoanType())
                .withBalance(ExactCurrencyAmount.inNOK(loanDetails.getBalance()))
                .withInterestRate(loanDetails.getInterestRate())
                .setApplicants(loanDetails.getAplicants())
                .setInitialBalance(ExactCurrencyAmount.inNOK(loanDetails.getInitialBalance()))
                .setAmortized(ExactCurrencyAmount.inNOK(loanDetails.getAmortized()))
                .setLoanNumber((StringUtils.removeNonAlphaNumeric(formattedNumber)))
                .setInitialDate(loanDetails.getInitialDate())
                .setNumMonthsBound(loanDetails.getNumMonthsBounds())
                .setMonthlyAmortization(
                        ExactCurrencyAmount.inNOK(loanDetails.getMonthlyAmortization()))
                .build();
    }

    private Type getLoanType() {
        // this is very basic mapping and will be improved as more data will come
        if (type.equals("LOAN")) {
            return Type.CREDIT;
        }
        return Type.OTHER;
    }
}
