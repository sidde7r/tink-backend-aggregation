package se.tink.backend.aggregation.nxgen.core.account.loan;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Loan;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.loan.util.LoanInterpreter;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;

public class LoanDetails {
    private static final AggregationLogger log = new AggregationLogger(LoanDetails.class);

    private final ExactCurrencyAmount amortized;
    private final ExactCurrencyAmount monthlyAmortization;
    private final ExactCurrencyAmount initialBalance;
    private final Date initialDate;
    private final String loanNumber;
    private final int numMonthsBound;
    private final Date nextDayOfTermsChange;
    private final Type type;
    private final String security;
    private final List<String> applicants;
    private final boolean coApplicant;

    private LoanDetails(
            ExactCurrencyAmount amortized,
            ExactCurrencyAmount monthlyAmortization,
            ExactCurrencyAmount initialBalance,
            Date initialDate,
            String loanNumber,
            int numMonthsBound,
            Date nextDayOfTermsChange,
            Type type,
            String security,
            List<String> applicants,
            boolean coApplicant) {
        this.amortized = amortized;
        this.monthlyAmortization = monthlyAmortization;
        this.initialBalance = initialBalance;
        this.initialDate = initialDate;
        this.loanNumber = loanNumber;
        this.numMonthsBound = numMonthsBound;
        this.nextDayOfTermsChange = nextDayOfTermsChange;
        this.type = type;
        this.security = security;
        this.applicants = applicants;
        this.coApplicant = coApplicant;
    }

    public static Builder builder(Type type) {
        return new Builder(type);
    }

    @Deprecated
    public Amount getAmortized() {
        return Optional.ofNullable(amortized)
                .map(a -> new Amount(a.getCurrencyCode(), a.getDoubleValue()))
                .orElse(null);
    }

    public ExactCurrencyAmount getExactAmortized() {
        return amortized;
    }

    private Double calculateAmortizedValue(LoanAccount account) {
        if (amortized != null) {
            return amortized.getDoubleValue();
        }

        if (initialBalance != null) {
            if (!Objects.equals(
                    initialBalance.getCurrencyCode(),
                    account.getExactBalance().getCurrencyCode())) {
                log.warn(
                        String.format(
                                "Detected Multiple loan currencies {balance: %s, initialBalance: %s}",
                                account.getExactBalance().getCurrencyCode(),
                                initialBalance.getCurrencyCode()));
            }

            return initialBalance.getDoubleValue() - account.getExactBalance().getDoubleValue();
        }

        return null;
    }

    @Deprecated
    public Amount getMonthlyAmortization() {
        return Optional.ofNullable(monthlyAmortization)
                .map(
                        e ->
                                new Amount(
                                        monthlyAmortization.getCurrencyCode(),
                                        monthlyAmortization.getDoubleValue()))
                .orElse(null);
    }

    public ExactCurrencyAmount getExactMonthlyAmortization() {
        return monthlyAmortization;
    }

    private Double calculateMonthlyAmortizationValue(LoanAccount account) {
        if (monthlyAmortization != null) {
            return monthlyAmortization.getDoubleValue();
        }
        Double amortizedValue = calculateAmortizedValue(account);

        if (initialDate == null || amortizedValue == null) {
            return null;
        }

        long monthsAmortized = DateUtils.getCalendarMonthsBetween(initialDate, new Date());
        return monthsAmortized != 0 ? amortizedValue / monthsAmortized : 0;
    }

    public Amount getInitialBalance() {
        return Optional.ofNullable(initialBalance)
                .map(
                        i ->
                                new Amount(
                                        initialBalance.getCurrencyCode(),
                                        initialBalance.getDoubleValue()))
                .orElse(null);
    }

    private Double getInitialBalanceValue() {
        return Optional.ofNullable(initialBalance)
                .map(ExactCurrencyAmount::getDoubleValue)
                .orElse(null);
    }

    public Date getInitialDate() {
        return initialDate;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public int getNumMonthsBound() {
        return numMonthsBound;
    }

    public Date getNextDayOfTermsChange() {
        return nextDayOfTermsChange;
    }

    public Type getType() {
        return type;
    }

    public String getSecurity() {
        return security;
    }

    public List<String> getApplicants() {
        return Optional.ofNullable(applicants)
                .<List<String>>map(ImmutableList::copyOf)
                .orElseGet(Collections::emptyList);
    }

    public boolean hasCoApplicant() {
        return getApplicants().size() > 1 || coApplicant;
    }

    public Loan toSystemLoan(LoanAccount account, LoanInterpreter interpreter) {
        Loan loan = new Loan();

        loan.setBalance(account.getExactBalance().getDoubleValue());
        loan.setInterest(account.getInterestRate());
        loan.setName(account.getName());
        loan.setLoanNumber(
                Strings.isNullOrEmpty(loanNumber) ? account.getAccountNumber() : loanNumber);
        loan.setAmortized(calculateAmortizedValue(account));
        loan.setMonthlyAmortization(calculateMonthlyAmortizationValue(account));
        loan.setInitialBalance(getInitialBalanceValue());
        loan.setInitialDate(initialDate);
        loan.setNumMonthsBound(numMonthsBound);
        loan.setNextDayOfTermsChange(nextDayOfTermsChange);

        Type type =
                Type.DERIVE_FROM_NAME.equals(getType())
                        ? interpreter.interpretLoanType(loan.getName())
                        : getType();

        loan.setType(type.toSystemType());

        se.tink.backend.aggregation.agents.models.LoanDetails loanDetails =
                new se.tink.backend.aggregation.agents.models.LoanDetails();
        loanDetails.setLoanSecurity(security);
        loanDetails.setCoApplicant(hasCoApplicant());
        loanDetails.setApplicants(applicants);

        loan.setLoanDetails(loanDetails);

        return loan;
    }

    public enum Type {
        MORTGAGE(Loan.Type.MORTGAGE),
        BLANCO(Loan.Type.BLANCO),
        MEMBERSHIP(Loan.Type.MEMBERSHIP),
        VEHICLE(Loan.Type.VEHICLE),
        LAND(Loan.Type.LAND),
        STUDENT(Loan.Type.STUDENT),
        OTHER(Loan.Type.OTHER),
        DERIVE_FROM_NAME(null);

        private final Loan.Type type;

        Type(Loan.Type type) {
            this.type = type;
        }

        public Loan.Type toSystemType() {
            Preconditions.checkNotNull(
                    type,
                    "System can`t accept null type. "
                            + "Type must be set explicitly or be derived using LoanInterpreter.");
            return type;
        }
    }

    public static class Builder {
        private ExactCurrencyAmount amortized;
        private ExactCurrencyAmount monthlyAmortization;
        private ExactCurrencyAmount initialBalance;
        private Date initialDate;
        private String loanNumber;
        private int numMonthsBound;
        private Date nextDayOfTermsChange;
        private Type type;
        private String security;
        private List<String> applicants;
        private boolean coApplicant;

        public Builder(Type type) {
            Preconditions.checkNotNull(type, String.format("%s", type));
            this.type = type;
        }

        @Deprecated
        public Amount getAmortized() {
            return Optional.ofNullable(amortized)
                    .filter(a -> Objects.nonNull(a.getExactValue()))
                    .map(a -> new Amount(a.getCurrencyCode(), a.getDoubleValue()))
                    .orElse(null);
        }

        @Deprecated
        public Builder setAmortized(Amount amortized) {
            this.amortized =
                    ExactCurrencyAmount.of(amortized.toBigDecimal(), amortized.getCurrency());
            return this;
        }

        public Builder setAmortized(ExactCurrencyAmount amortized) {
            this.amortized = amortized;
            return this;
        }

        public ExactCurrencyAmount getExactAmortized() {
            return amortized;
        }

        @Deprecated
        public Amount getMonthlyAmortization() {
            return Optional.ofNullable(monthlyAmortization)
                    .filter(m -> Objects.nonNull(m.getExactValue()))
                    .map(m -> new Amount(m.getCurrencyCode(), m.getDoubleValue()))
                    .orElse(null);
        }

        @Deprecated
        public Builder setMonthlyAmortization(Amount monthlyAmortization) {
            this.monthlyAmortization =
                    ExactCurrencyAmount.of(
                            monthlyAmortization.toBigDecimal(), monthlyAmortization.getCurrency());
            return this;
        }

        public Builder setMonthlyAmortization(ExactCurrencyAmount monthlyAmortization) {
            this.monthlyAmortization = monthlyAmortization;
            return this;
        }

        public ExactCurrencyAmount getExactMonthlyAmortization() {
            return monthlyAmortization;
        }

        @Deprecated
        public Amount getInitialBalance() {
            return Optional.ofNullable(initialBalance)
                    .filter(i -> Objects.nonNull(i.getExactValue()))
                    .map(i -> new Amount(i.getCurrencyCode(), i.getDoubleValue()))
                    .orElse(null);
        }

        @Deprecated
        public Builder setInitialBalance(Amount initialBalance) {
            this.initialBalance =
                    ExactCurrencyAmount.of(
                            initialBalance.toBigDecimal(), initialBalance.getCurrency());
            return this;
        }

        public Builder setInitialBalance(ExactCurrencyAmount initialBalance) {
            this.initialBalance = initialBalance;
            return this;
        }

        public ExactCurrencyAmount getExactInitialBalance() {
            return initialBalance;
        }

        public Date getInitialDate() {
            return initialDate;
        }

        public Builder setInitialDate(Date initialDate) {
            this.initialDate = initialDate;
            return this;
        }

        public String getLoanNumber() {
            return loanNumber;
        }

        public Builder setLoanNumber(String loanNumber) {
            this.loanNumber = loanNumber;
            return this;
        }

        public int getNumMonthsBound() {
            return numMonthsBound;
        }

        public Builder setNumMonthsBound(int numMonthsBound) {
            this.numMonthsBound = numMonthsBound;
            return this;
        }

        public Date getNextDayOfTermsChange() {
            return nextDayOfTermsChange;
        }

        public Builder setNextDayOfTermsChange(Date nextDayOfTermsChange) {
            this.nextDayOfTermsChange = nextDayOfTermsChange;
            return this;
        }

        public Type getType() {
            return type;
        }

        public String getSecurity() {
            return security;
        }

        public Builder setSecurity(String security) {
            this.security = security;
            return this;
        }

        public List<String> getApplicants() {
            return applicants;
        }

        public Builder setApplicants(List<String> applicants) {
            this.applicants = applicants;
            return this;
        }

        public boolean hasCoApplicant() {
            return coApplicant;
        }

        public Builder setCoApplicant(boolean coApplicant) {
            this.coApplicant = coApplicant;
            return this;
        }

        public LoanDetails build() {
            return new LoanDetails(
                    getExactAmortized(),
                    getExactMonthlyAmortization(),
                    getExactInitialBalance(),
                    getInitialDate(),
                    getLoanNumber(),
                    getNumMonthsBound(),
                    getNextDayOfTermsChange(),
                    getType(),
                    getSecurity(),
                    getApplicants(),
                    hasCoApplicant());
        }
    }
}
