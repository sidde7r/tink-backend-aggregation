package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItem;

@Generated("com.robohorse.robopojogenerator")
public class AccountsResponse{

	@JsonProperty("accounts")
	private List<AccountsItem> accounts;

	public List<AccountsItem> getAccounts(){
		return accounts;
	}
}