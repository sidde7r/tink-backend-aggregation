package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.Accounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsEntity {
    @JsonProperty("OwnAccounts")
    private List<OwnAccountsEntity> ownAccounts;
    @JsonProperty("JointAccounts")
    private List<OwnAccountsEntity> jointAccounts;
    @JsonProperty("MinorsAccounts")
    private List<OwnAccountsEntity> minorsAccounts;

    public List<OwnAccountsEntity> getOwnAccounts() {
        return ownAccounts;
    }

    public List<OwnAccountsEntity> getJointAccounts() {
        return jointAccounts;
    }

    public List<OwnAccountsEntity> getMinorsAccounts() {
        return minorsAccounts;
    }
}
