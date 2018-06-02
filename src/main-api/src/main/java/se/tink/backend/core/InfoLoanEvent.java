package se.tink.backend.core;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.Map;
import se.tink.libraries.i18n.Catalog;

// NOTE: Title strings are automatically localized
public class InfoLoanEvent extends LoanEvent {

    public InfoLoanEvent(){}
    public InfoLoanEvent(
            String accountId,
            Date timestamp,
            Loan.Type loanType,
            Date nextDayOfTermsChange,
            String provider,
            String credentials,
            String loanName,
            Double currentInterestRate,
            Double balance,
            String title,
            Catalog catalog) {
        Map<String, Object> properties = Maps.newHashMap();

        setType(Type.INFO);
        setLoanType(loanType);
        setAccountId(accountId);
        setTimestamp(timestamp);
        setTitle(catalog.getString(title));
        setInterest(currentInterestRate);
        setBalance(balance);
        setProvider(provider);
        setCredentials(credentials);
        setNextDayOfTermsChange(nextDayOfTermsChange);

        properties.put("name", loanName);
        setInterestRateChange(0.0);

        setProperties(properties);
    }
}

