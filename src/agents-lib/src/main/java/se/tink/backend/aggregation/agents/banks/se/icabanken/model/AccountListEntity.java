package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountListEntity {
	@JsonProperty("OwnAccounts")
	private List<AccountEntity> ownAccounts;
    @JsonProperty("JointAccounts")
    private List<AccountEntity> jointAccounts;
    @JsonProperty("MinorsAccounts")
    private List<AccountEntity> minorsAccounts;
    
	public List<AccountEntity> getOwnAccounts() {
		return ownAccounts;
	}

	public void setOwnAccounts(List<AccountEntity> ownAccounts) {
		this.ownAccounts = ownAccounts;
	}

    public List<AccountEntity> concatenateAccounts() {
        List<AccountEntity> accounts = Lists.newArrayList();

        if (ownAccounts != null) {
            accounts.addAll(ownAccounts);
        }

        if (jointAccounts != null) {
            accounts.addAll(jointAccounts);
        }

        if (minorsAccounts != null) {
            accounts.addAll(minorsAccounts);
        }
        
        return accounts;
    }
}
