package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.loan.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
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
public class SBABLoansEntity {
    private List<BorrowersEntity> borrowers;
    private String contractor;
    private String discountType;
    private BigDecimal interestRateDiscount;
    private boolean invoicePostponed;
    private BigDecimal loanAmount;
    private String loanNumber;
    private LoanObjectEntity loanObject;
    private String loanStatus;
    private LoanTermsEntity loanTerms;
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
                                .withBalance(getLoanAmount().negate())
                                .withInterestRate(getLoanTerms().getInterestRate().doubleValue())
                                .setNumMonthsBound(calculateNumMonthsBound())
                                .setAmortized(getAmortizied())
                                .setApplicants(getApplicants())
                                .setCoApplicant(hasCoApplicants())
                                .setNextDayOfTermsChange(getNextDayOfTermsChange())
                                .setInitialBalance(getOriginalLoanAmount())
                                .setLoanNumber(loanNumber)
                                .setInitialDate(getInitialDate())
                                .build())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(loanNumber)
                                .withAccountNumber(loanNumber)
                                .withAccountName(getAccountName())
                                .addIdentifier(new SwedishIdentifier(loanNumber))
                                .build())
                .build();
    }

    private ExactCurrencyAmount getLoanAmount() {
        return ExactCurrencyAmount.of(loanAmount, SBABConstants.CURRENCY);
    }

    private ExactCurrencyAmount getAmortizied() {
        return ExactCurrencyAmount.of(
                getLoanTerms().getAmortizationAmount(), SBABConstants.CURRENCY);
    }

    private Type getLoanType() {
        return SBABConstants.LOAN_TYPES.get(loanType);
    }

    // SBAB does not provide with any loan account names so this is parsing the loan type instead.
    // e.g. "loanType": "MORTGAGE_LOAN" will return "MORTGAGE"
    private String getAccountName() {
        return loanType.split("_")[0];
    }

    private List<String> getApplicants() {
        return borrowers.stream().map(BorrowersEntity::getDisplayName).collect(Collectors.toList());
    }

    private boolean hasCoApplicants() {
        return getApplicants().size() > 1;
    }

    private LocalDate getNextDayOfTermsChange() {
        final String changeOfConditionDate = getLoanTerms().getChangeOfConditionDate();
        if (changeOfConditionDate == null) {
            return null;
        }
        return LocalDate.parse(changeOfConditionDate);
    }

    private LocalDate getInitialDate() {
        final String startDate = getLoanTerms().getStartDate();
        if (startDate == null) {
            return null;
        }
        return LocalDate.parse(startDate);
    }

    private ExactCurrencyAmount getOriginalLoanAmount() {
        return ExactCurrencyAmount.of(originalLoanAmount, SBABConstants.CURRENCY);
    }

    // values comes as for example "THREE_MONTHS" or "THREE_YEARS"
    private Integer calculateNumMonthsBound() {
        final String fixedInterestPeriod = getLoanTerms().getFixedInterestPeriod();
        if (fixedInterestPeriod == null) {
            return null;
        }
        final String numberOfMonthsOrYearsAsString = fixedInterestPeriod.split("_")[0];
        final String monthsOrYears = fixedInterestPeriod.split("_")[1];
        final Integer numberOfMonthsOrYears =
                SBABConstants.INTEREST_NUMBERS.get(numberOfMonthsOrYearsAsString);

        if (monthsOrYears.contains("YEAR")) {
            return numberOfMonthsOrYears * 12;
        }
        return numberOfMonthsOrYears;
    }
}
