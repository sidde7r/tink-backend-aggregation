package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailedPermissions {
    private boolean changeFromAccount;
    private boolean changeDate;
    private boolean changeAmount;
    private boolean changeMessage;

    public boolean isChangeFromAccount() {
        return changeFromAccount;
    }

    public boolean isChangeDate() {
        return changeDate;
    }

    public boolean isChangeAmount() {
        return changeAmount;
    }

    public boolean isChangeMessage() {
        return changeMessage;
    }
}
