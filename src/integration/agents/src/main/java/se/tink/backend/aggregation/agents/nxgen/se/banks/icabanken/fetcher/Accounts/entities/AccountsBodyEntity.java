package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsBodyEntity {
    @JsonProperty("OwnAccountsTotalCurrentAmount")
    private double ownAccountsTotalCurrentAmount;

    @JsonProperty("OwnAccountsTotalAvailableAmount")
    private double ownAccountsTotalAvailableAmount;

    @JsonProperty("JointAccountsTotalCurrentAmount")
    private double jointAccountsTotalCurrentAmount;

    @JsonProperty("JointAccountsTotalAvailableAmount")
    private double jointAccountsTotalAvailableAmount;

    @JsonProperty("MinorsAccountsTotalCurrentAmount")
    private double minorsAccountsTotalCurrentAmount;

    @JsonProperty("MinorsAccountsTotalAvailableAmount")
    private double minorsAccountsTotalAvailableAmount;

    @JsonProperty("DefaultAccountIdForPayments")
    private String defaultAccountIdForPayments;

    @JsonProperty("DefaultAccountIdForTransfers")
    private String defaultAccountIdForTransfers;

    @JsonProperty("DefaultAccountIdForEgiros")
    private String defaultAccountIdForEgiros;

    @JsonProperty("DefaultAccountIdForInvest")
    private String defaultAccountIdForInvest;

    @JsonProperty("Accounts")
    private AccountsEntity accounts;

    public double getOwnAccountsTotalCurrentAmount() {
        return ownAccountsTotalCurrentAmount;
    }

    public double getOwnAccountsTotalAvailableAmount() {
        return ownAccountsTotalAvailableAmount;
    }

    public double getJointAccountsTotalCurrentAmount() {
        return jointAccountsTotalCurrentAmount;
    }

    public double getJointAccountsTotalAvailableAmount() {
        return jointAccountsTotalAvailableAmount;
    }

    public double getMinorsAccountsTotalCurrentAmount() {
        return minorsAccountsTotalCurrentAmount;
    }

    public double getMinorsAccountsTotalAvailableAmount() {
        return minorsAccountsTotalAvailableAmount;
    }

    public String getDefaultAccountIdForPayments() {
        return defaultAccountIdForPayments;
    }

    public String getDefaultAccountIdForTransfers() {
        return defaultAccountIdForTransfers;
    }

    public String getDefaultAccountIdForEgiros() {
        return defaultAccountIdForEgiros;
    }

    public String getDefaultAccountIdForInvest() {
        return defaultAccountIdForInvest;
    }

    public AccountsEntity getAccounts() {
        return accounts;
    }
}
