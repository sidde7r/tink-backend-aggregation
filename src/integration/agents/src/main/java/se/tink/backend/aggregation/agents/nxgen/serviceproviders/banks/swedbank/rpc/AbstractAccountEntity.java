package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractAccountEntity {
    protected String id;
    protected String name;
    protected String accountNumber;
    protected String clearingNumber;
    protected String fullyFormattedNumber;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
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

    public boolean isSameAccount(AbstractAccountEntity accountToCompare) {
        return clearingNumber.equalsIgnoreCase(accountToCompare.getClearingNumber())
                && accountNumber.equalsIgnoreCase(accountToCompare.getAccountNumber());
    }
}
