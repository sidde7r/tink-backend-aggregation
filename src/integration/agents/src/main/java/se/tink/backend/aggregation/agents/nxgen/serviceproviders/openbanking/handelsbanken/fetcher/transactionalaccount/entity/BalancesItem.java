package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class BalancesItem{

	@JsonProperty("amount")
	private Amount amount;

	@JsonProperty("balanceType")
	private String balanceType;

	public Amount getAmount(){
		return amount;
	}

	public String getBalanceType(){
		return balanceType;
	}
}