package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.einvoice.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FromAccountGroupEntity {
    private List<String> scopes;
    private String currencyCode;
    private String amount;
    private String name;
    private String id;
    private String accountNumber;
    private String clearingNumber;
    private String fullyFormattedNumber;

    public List<String> getScopes() {
        return scopes;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getAmount() {
        return amount;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public String getFullyFormattedNumber() {
        return fullyFormattedNumber;
    }
}
