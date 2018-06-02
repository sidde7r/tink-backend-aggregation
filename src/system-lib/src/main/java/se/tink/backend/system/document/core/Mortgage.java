package se.tink.backend.system.document.core;

import java.util.List;
import java.util.Optional;

public class Mortgage {

    private final Optional<Security> security;
    private final boolean hasAmortizationRequirement;
    private final Optional<String> accountNumber;
    private final List<String> loanNumbers;

    public Mortgage(Optional<Security> security, boolean hasAmortizationRequirement, Optional<String> accountNumber, List<String> loanNumbers) {
        this.security = security;
        this.hasAmortizationRequirement = hasAmortizationRequirement;
        this.accountNumber = accountNumber;
        this.loanNumbers = loanNumbers;
    }

    public Optional<Security> getSecurity() {
        return security;
    }

    public boolean getHasAmortizationRequirement() {
        return hasAmortizationRequirement;
    }

    public Optional<String> getAccountNumber() { return accountNumber; }

    public List<String> getLoanNumbers() {
        return loanNumbers;
    }
}
