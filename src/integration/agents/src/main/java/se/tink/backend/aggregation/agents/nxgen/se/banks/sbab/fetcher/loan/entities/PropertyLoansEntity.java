package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class PropertyLoansEntity {
    private List<BorrowersEntity> borrowers;
    private String contractor;
    private String discountType;
    private BigDecimal interestRateDiscount;
    private boolean invoicePostponed;
    private String legacyLoanNumber;
    private BigDecimal loanAmount;
    private String loanNumber;
    private LoanObjectEntity loanObjectEntity;
    private String loanStatus;
    private LoanTermsEntity loanTermsEntity;
    private String loanType;
    private BigDecimal numberOfBorrowers;
    private String objectType;
    private BigDecimal originalLoanAmount;
    private BigDecimal participationShare;
    private Date paymentDate;

    public LoanAccount toTinkLoanAccount() {
        return LoanAccount.nxBuilder()
                .withLoanDetails(
                        LoanModule.builder()
                                .withType(getLoanType())
                                .withBalance(getLoanAmount())
                                .withInterestRate(getInterestRateDiscount().doubleValue())
                                .setInitialBalance(getOriginalLoanAmount())
                                .setLoanNumber(getLegacyLoanNumber())
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loanNumber)
                                .withAccountNumber(loanNumber)
                                .withAccountName(contractor)
                                .addIdentifier(new SwedishIdentifier(loanNumber))
                                .build())
                .build();
    }

    public ExactCurrencyAmount getLoanAmount() {
        return ExactCurrencyAmount.of(loanAmount, SBABConstants.CURRENCY);
    }

    public Type getLoanType() {
        return SBABConstants.LOAN_TYPES.get(loanType);
    }

    public ExactCurrencyAmount getOriginalLoanAmount() {
        return ExactCurrencyAmount.of(originalLoanAmount, SBABConstants.CURRENCY);
    }
}
