package se.tink.backend.export.model.submodels;

import java.util.Date;
import java.util.List;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportLoan implements DefaultSetter {

    private final String accountNumber;
    private final String applicants;
    private final String loanSecurity;
    private final String loanNumber;
    private final Double amortized;
    private final Double balance;
    private final Double initialBalance;
    private final Double interest;
    private final Double monthlyAmortization;
    private final String name;
    private final String initialDate;
    private final String nextDayOfTermsChange;
    private final Integer monthsBound;
    private final String providerName;
    private final String type;
    private final String loanResponse; // Todo: deserialize and format correctly

    public ExportLoan (
            String accountNumber,
            List<String> applicants,
            String loanSecurity,
            String loanNumber,
            Double amortized,
            Double balance,
            Double initialBalance,
            Double interest,
            Double monthlyAmortization,
            String name,
            Date initialDate,
            Date nextDayOfTermsChange,
            Integer monthsBound,
            String providerName,
            String type,
            String loanResponse) {
        this.accountNumber = accountNumber;
        this.applicants = String.join(", ", applicants);
        this.loanSecurity = loanSecurity;
        this.loanNumber = loanNumber;
        this.amortized = amortized;
        this.balance = balance;
        this.initialBalance = initialBalance;
        this.interest = interest;
        this.monthlyAmortization = monthlyAmortization;
        this.name = name;
        this.initialDate = notNull(initialDate);
        this.nextDayOfTermsChange = notNull(nextDayOfTermsChange);
        this.monthsBound = monthsBound;
        this.providerName = providerName;
        this.type = type;
        this.loanResponse = loanResponse;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getApplicants() {
        return applicants;
    }

    public String getLoanSecurity() {
        return loanSecurity;
    }

    public String getLoanNumber() {
        return loanNumber;
    }

    public Double getAmortized() {
        return amortized;
    }

    public Double getBalance() {
        return balance;
    }

    public Double getInitialBalance() {
        return initialBalance;
    }

    public String getInitialDate() {
        return initialDate;
    }

    public Double getInterest() {
        return interest;
    }

    public Double getMonthlyAmortization() {
        return monthlyAmortization;
    }

    public String getName() {
        return name;
    }

    public String getNextDayOfTermsChange() {
        return nextDayOfTermsChange;
    }

    public Integer getMonthsBound() {
        return monthsBound;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getType() {
        return type;
    }

    public String getLoanResponse() {
        return loanResponse;
    }
}
