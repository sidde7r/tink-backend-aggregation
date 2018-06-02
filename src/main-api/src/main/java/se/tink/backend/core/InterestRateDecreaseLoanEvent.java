package se.tink.backend.core;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.Map;
import se.tink.libraries.i18n.Catalog;

public class InterestRateDecreaseLoanEvent extends LoanEvent {
    public InterestRateDecreaseLoanEvent(
            String accountId,
            Date timestamp,
            Loan.Type loanType,
            Date nextDayOfTermsChange,
            String provider,
            String credentials,
            Double change,
            Double currentInterestRate,
            Double balance,
            Catalog catalog) {
        Map<String, Object> properties = Maps.newHashMap();

        setType(Type.INTEREST_RATE_DECREASE);
        setLoanType(loanType);
        setAccountId(accountId);
        setTimestamp(timestamp);
        setTitle(catalog.getString("Your rate has decreased"));
        setInterest(currentInterestRate);
        setBalance(balance);
        setInterestRateChange(change);
        setProvider(provider);
        setCredentials(credentials);
        setNextDayOfTermsChange(nextDayOfTermsChange);
        setProperties(properties);
    }
}
