package se.tink.backend.aggregation.nxgen.core.account;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.amount.Amount;
import se.tink.backend.system.rpc.Loan;
import se.tink.libraries.date.DateUtils;

public class LoanDetails {
    private static final AggregationLogger log = new AggregationLogger(LoanDetails.class);

    private final Amount amortized;
    private final Amount monthlyAmortization;
    private final Amount initialBalance;
    private final Date initialDate;
    private final String loanNumber;
    private final int numMonthsBound;
    private final Date nextDayOfTermsChange;
    private final Type type;
    private final String security;
    private final List<String> applicants;
    private final boolean coApplicant;

    private LoanDetails() {
        this(null, null, null, null, null, 0, null, null, null, null, false);
    }

    private LoanDetails(Amount amortized, Amount monthlyAmortization, Amount initialBalance,
            Date initialDate, String loanNumber, int numMonthsBound, Date nextDayOfTermsChange, Type type,
            String security, List<String> applicants, boolean coApplicant) {
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

    public Amount getAmortized() {
        if (amortized != null) {
            return new Amount(amortized.getCurrency(), amortized.getValue());
        }
        return null;
    }

    private Double calculateAmortizedValue(LoanAccount account) {
        if (amortized != null) {
            return amortized.getValue();
        }

        if (initialBalance != null) {
            if (!Objects.equals(initialBalance.getCurrency(), account.getBalance().getCurrency())) {
                log.warn(String.format("Detected Multiple loan currencies {balance: %s, initialBalance: %s}",
                        account.getBalance().getCurrency(), initialBalance.getCurrency()));
            }

            return initialBalance.getValue() - account.getBalance().getValue();
        }

        return null;
    }

    public Amount getMonthlyAmortization() {
        if (monthlyAmortization != null) {
            return Amount.createFromAmount(monthlyAmortization).orElse(null);
        }
        return null;
    }

    private Double calculateMonthlyAmortizationValue(LoanAccount account) {
        if (monthlyAmortization != null && monthlyAmortization.getValue() != null) {
            return monthlyAmortization.getValue();
        }
        Double amortizedValue = calculateAmortizedValue(account);

        if (initialDate == null || amortizedValue == null) {
            return null;
        }

        long monthsAmortized = DateUtils.getCalendarMonthsBetween(initialDate, new Date());
        return monthsAmortized != 0 ? amortizedValue / monthsAmortized : 0;
    }

    public Amount getInitialBalance() {
        if (initialBalance != null) {
            return new Amount(initialBalance.getCurrency(), initialBalance.getValue());
        }
        return null;
    }

    private Double getInitialBalanceValue() {
        return initialBalance != null ? initialBalance.getValue() : null;
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
        return applicants != null ? ImmutableList.copyOf(applicants) : Collections.emptyList();
    }

    public boolean hasCoApplicant() {
        return getApplicants().size() > 1 || coApplicant;
    }

    public Loan toSystemLoan(LoanAccount account, LoanInterpreter interpreter) {
        Loan loan = new Loan();

        loan.setBalance(account.getBalance().getValue());
        loan.setInterest(account.getInterestRate());
        loan.setName(account.getName());
        loan.setLoanNumber(Strings.isNullOrEmpty(loanNumber) ? account.getAccountNumber() : loanNumber);
        loan.setAmortized(calculateAmortizedValue(account));
        loan.setMonthlyAmortization(calculateMonthlyAmortizationValue(account));
        loan.setInitialBalance(getInitialBalanceValue());
        loan.setInitialDate(initialDate);
        loan.setNumMonthsBound(numMonthsBound);
        loan.setNextDayOfTermsChange(nextDayOfTermsChange);

        Type type = Type.DERIVE_FROM_NAME.equals(getType()) ?
                interpreter.interpretLoanType(loan.getName()) :
                getType();

        loan.setType(type.toSystemType());

        se.tink.backend.system.rpc.LoanDetails loanDetails = new se.tink.backend.system.rpc.LoanDetails();
        loanDetails.setLoanSecurity(security);
        loanDetails.setCoApplicant(hasCoApplicant());
        loanDetails.setApplicants(applicants);

        loan.setLoanDetails(loanDetails);

        return loan;
    }

    public static Builder builder(Type type) {
        return new Builder(type);
    }

    public static class Builder {
        private Amount amortized;
        private Amount monthlyAmortization;
        private Amount initialBalance;
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

        public Amount getAmortized() {
            if (amortized != null) {
                return amortized.getValue() != null ? amortized : null;
            }
            return null;
        }

        public Builder setAmortized(Amount amortized) {
            this.amortized = amortized;
            return this;
        }

        public Amount getMonthlyAmortization() {
            if (monthlyAmortization != null) {
                return monthlyAmortization.getValue() != null ? monthlyAmortization : null;
            }
            return null;
        }

        public Builder setMonthlyAmortization(Amount monthlyAmortization) {
            this.monthlyAmortization = monthlyAmortization;
            return this;
        }

        public Amount getInitialBalance() {
            if (initialBalance != null) {
                return initialBalance.getValue() != null ? initialBalance : null;
            }
            return null;
        }

        public Builder setInitialBalance(Amount initialBalance) {
            this.initialBalance = initialBalance;
            return this;
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
            return new LoanDetails(getAmortized(), getMonthlyAmortization(), getInitialBalance(),
                    getInitialDate(), getLoanNumber(), getNumMonthsBound(), getNextDayOfTermsChange(), getType(),
                    getSecurity(), getApplicants(), hasCoApplicant());
        }
    }

    public enum Type {
        MORTGAGE(Loan.Type.MORTGAGE), BLANCO(Loan.Type.BLANCO), MEMBERSHIP(Loan.Type.MEMBERSHIP),
        VEHICLE(Loan.Type.VEHICLE), LAND(Loan.Type.LAND), STUDENT(Loan.Type.STUDENT),
        OTHER(Loan.Type.OTHER), DERIVE_FROM_NAME(null);

        private final Loan.Type type;

        Type(Loan.Type type) {
            this.type = type;
        }

        public Loan.Type toSystemType() {
            Preconditions.checkNotNull(type,
                    "System can`t accept null type. "
                    + "Type must be set explicitly or be derived using LoanInterpreter.");
            return type;
        }
    }
}
