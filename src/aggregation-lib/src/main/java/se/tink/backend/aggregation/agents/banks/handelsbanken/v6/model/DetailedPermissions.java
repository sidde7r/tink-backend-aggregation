package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailedPermissions {
    private boolean changeFromAccount;
    private boolean changeDate;
    private boolean changeAmount;
    private boolean changeMessage;

    public boolean isChangeFromAccount() {
        return changeFromAccount;
    }

    public void setChangeFromAccount(boolean changeFromAccount) {
        this.changeFromAccount = changeFromAccount;
    }

    public boolean isChangeDate() {
        return changeDate;
    }

    public void setChangeDate(boolean changeDate) {
        this.changeDate = changeDate;
    }

    public boolean isChangeAmount() {
        return changeAmount;
    }

    public void setChangeAmount(boolean changeAmount) {
        this.changeAmount = changeAmount;
    }

    public boolean isChangeMessage() {
        return changeMessage;
    }

    public void setChangeMessage(boolean changeMessage) {
        this.changeMessage = changeMessage;
    }
}
