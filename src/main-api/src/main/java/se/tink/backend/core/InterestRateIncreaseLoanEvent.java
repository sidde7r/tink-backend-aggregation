package se.tink.backend.core;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.Map;
import se.tink.libraries.i18n.Catalog;

public class InterestRateIncreaseLoanEvent extends LoanEvent {
    public InterestRateIncreaseLoanEvent(
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

        setType(Type.INTEREST_RATE_INCREASE);
        setLoanType(loanType);
        setAccountId(accountId);
        setTimestamp(timestamp);
        setTitle(catalog.getString("Your rate has increased"));
        setInterest(currentInterestRate);
        setBalance(balance);
        setInterestRateChange(change);
        setProvider(provider);
        setCredentials(credentials);
        setNextDayOfTermsChange(nextDayOfTermsChange);
        setProperties(properties);
    }
}

