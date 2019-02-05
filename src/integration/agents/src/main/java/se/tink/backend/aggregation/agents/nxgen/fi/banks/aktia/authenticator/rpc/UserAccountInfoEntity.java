package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserAccountInfoEntity {
    private String customerName;
    private boolean passwordUpdateRequired;
    private boolean accountLocked;
    private String customerType;

    public String getCustomerName() {
        return customerName;
    }

    public boolean isPasswordUpdateRequired() {
        return passwordUpdateRequired;
    }

    public boolean isAccountLocked() {
        return accountLocked;
    }

    public String getCustomerType() {
        return customerType;
    }
}
