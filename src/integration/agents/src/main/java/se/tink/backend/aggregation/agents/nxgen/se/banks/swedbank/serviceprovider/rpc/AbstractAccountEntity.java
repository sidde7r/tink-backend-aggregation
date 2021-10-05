package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public abstract class AbstractAccountEntity {
    protected String id;
    protected String name;
    protected String accountNumber;
    protected String clearingNumber;
    protected String fullyFormattedNumber;

    public boolean isSameAccount(AbstractAccountEntity accountToCompare) {
        return clearingNumber.equalsIgnoreCase(accountToCompare.getClearingNumber())
                && accountNumber.equalsIgnoreCase(accountToCompare.getAccountNumber());
    }
}
