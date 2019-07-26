package se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.BalanceStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.InterestStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.LoanModuleBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.builder.TypeStep;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;

public final class LoanModule {

    // Mandatory properties
    private final ExactCurrencyAmount balance;
    private final double interestRate;
    private final Type loanType;

    // Optional properties
    private final ExactCurrencyAmount amortized;
    private final ExactCurrencyAmount monthlyAmortization;
    private final ExactCurrencyAmount initialBalance;
    private final LocalDate initialDate;
    private final String loanNumber;
    private final int numMonthsBound;
    private final LocalDate nextDayOfTermsChange;
    private final String security;
    private final List<String> applicants;
    private final boolean coApplicant;

    private LoanModule(Builder builder) {
        this.balance = builder.balance;
        this.interestRate = builder.interestRate;
        this.loanType = builder.loanType;
        this.amortized = builder.amortized;
        this.monthlyAmortization = builder.monthlyAmortization;
        this.initialBalance = builder.initialBalance;
        this.initialDate = builder.initialDate;
        this.loanNumber = builder.loanNumber;
        this.numMonthsBound = builder.numMonthsBound;
        this.nextDayOfTermsChange = builder.nextDayOfTermsChange;
        this.security = builder.security;
        this.applicants = builder.applicants;
        this.coApplicant = builder.coApplicant;
    }

    public static TypeStep<LoanModuleBuildStep> builder() {
        return new Builder();
    }

    public ExactCurrencyAmount getBalance() {
        return balance;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public Type getLoanType() {
        return loanType;
    }

    public ExactCurrencyAmount getAmortized() {
        return amortized;
    }

    public ExactCurrencyAmount getMonthlyAmortization() {
        return monthlyAmortization;
    }

    public ExactCurrencyAmount getInitialBalance() {
        return initialBalance;
    }

    public LocalDate getInitialDate() {
        return initialDate;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public int getNumMonthsBound() {
        return numMonthsBound;
    }

    public LocalDate getNextDayOfTermsChange() {
        return nextDayOfTermsChange;
    }

    public String getSecurity() {
        return security;
    }

    public List<String> getApplicants() {
        return applicants;
    }

    public boolean isCoApplicant() {
        return coApplicant;
    }

    public LoanDetails toLoanDetails() {
        return LoanDetails.builder(loanType)
                .setAmortized(amortized)
                .setApplicants(applicants)
                .setCoApplicant(coApplicant)
                .setInitialBalance(initialBalance)
                .setInitialDate(
                        Optional.ofNullable(initialDate)
                                .map(DateUtils::toJavaUtilDate)
                                .orElse(null))
                .setLoanNumber(loanNumber)
                .setMonthlyAmortization(monthlyAmortization)
                .setNextDayOfTermsChange(
                        Optional.ofNullable(nextDayOfTermsChange)
                                .map(DateUtils::toJavaUtilDate)
                                .orElse(null))
                .setNumMonthsBound(numMonthsBound)
                .setSecurity(security)
                .build();
    }

    private static class Builder
            implements TypeStep<LoanModuleBuildStep>,
                    BalanceStep<LoanModuleBuildStep>,
                    InterestStep<LoanModuleBuildStep>,
                    LoanModuleBuildStep {

        // Mandatory properties
        private ExactCurrencyAmount balance;
        private double interestRate;
        private Type loanType;

        // Optional properties
        private ExactCurrencyAmount amortized;
        private ExactCurrencyAmount monthlyAmortization;
        private ExactCurrencyAmount initialBalance;
        private LocalDate initialDate;
        private String loanNumber;
        private int numMonthsBound;
        private LocalDate nextDayOfTermsChange;
        private String security;
        private List<String> applicants;
        private boolean coApplicant;

        @Override
        public BalanceStep<LoanModuleBuildStep> withType(Type loanType) {
            Preconditions.checkNotNull(loanType, "LoanType must not be null.");
            this.loanType = loanType;
            return this;
        }

        @Override
        public InterestStep<LoanModuleBuildStep> withBalance(ExactCurrencyAmount balance) {
            Preconditions.checkNotNull(balance, "Amount must not be null.");
            this.balance = balance;
            return this;
        }

        @Override
        public LoanModuleBuildStep withInterestRate(double interestRate) {
            Preconditions.checkArgument(interestRate >= 0, "Interest Rate must not be negative.");
            this.interestRate = interestRate;
            return this;
        }

        @Override
        public LoanModuleBuildStep setAmortized(ExactCurrencyAmount amortized) {
            Preconditions.checkNotNull(amortized, "Amortized must not be null.");
            this.amortized = amortized;
            return this;
        }

        @Override
        public LoanModuleBuildStep setMonthlyAmortization(ExactCurrencyAmount monthlyAmortization) {
            Preconditions.checkNotNull(
                    monthlyAmortization, "MonthlyAmortization must not be null.");
            this.monthlyAmortization = monthlyAmortization;
            return this;
        }

        @Override
        public LoanModuleBuildStep setInitialBalance(ExactCurrencyAmount initialBalance) {
            Preconditions.checkNotNull(initialBalance, "InitialBalance must not be null.");
            this.initialBalance = initialBalance;
            return this;
        }

        @Override
        public LoanModuleBuildStep setInitialDate(LocalDate initialDate) {
            Preconditions.checkNotNull(initialDate, "InitialDate must not be null.");
            this.initialDate = initialDate;
            return this;
        }

        @Override
        public LoanModuleBuildStep setLoanNumber(String loanNumber) {
            Preconditions.checkNotNull(loanNumber, "LoanNumber must not be null.");
            this.loanNumber = loanNumber;
            return this;
        }

        @Override
        public LoanModuleBuildStep setNumMonthsBound(int numMonthsBound) {
            Preconditions.checkArgument(numMonthsBound >= 0, "Bound must not be negative.");
            this.numMonthsBound = numMonthsBound;
            return this;
        }

        @Override
        public LoanModuleBuildStep setNextDayOfTermsChange(LocalDate nextDayOfTermsChange) {
            Preconditions.checkNotNull(
                    nextDayOfTermsChange, "NextDayOfTermsChange must not be null.");
            this.nextDayOfTermsChange = nextDayOfTermsChange;
            return this;
        }

        @Override
        public LoanModuleBuildStep setSecurity(String security) {
            Preconditions.checkNotNull(security, "Security must not be null.");
            this.security = security;
            return this;
        }

        @Override
        public LoanModuleBuildStep setApplicants(List<String> applicants) {
            Preconditions.checkNotNull(applicants, "Applicants must not be null.");
            this.applicants = applicants;
            return this;
        }

        @Override
        public LoanModuleBuildStep setCoApplicant(boolean coApplicant) {
            this.coApplicant = coApplicant;
            return this;
        }

        @Override
        public LoanModule build() {
            return new LoanModule(this);
        }
    }
}
