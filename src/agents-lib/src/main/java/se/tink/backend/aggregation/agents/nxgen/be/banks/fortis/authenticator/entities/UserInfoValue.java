package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserInfoValue {
	private UserDataValue userData;
	private BranchData branchData;
	private CustomerData customerData;

	public UserDataValue getUserData(){
		return userData;
	}

	public BranchData getBranchData(){
		return branchData;
	}

	public CustomerData getCustomerData(){
		return customerData;
	}
}
