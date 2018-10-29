package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Balance {

	@JsonProperty("reserved_amount")
	private double reservedAmount;

	@JsonProperty("free_credit_amount")
	private double freeCreditAmount;

	@JsonProperty("currency_id")
	private int currencyId;

	@JsonProperty("free_amount")
	private double freeAmount;

	public void setReservedAmount(double reservedAmount){
		this.reservedAmount = reservedAmount;
	}

	public double getReservedAmount(){
		return reservedAmount;
	}

	public void setFreeCreditAmount(double freeCreditAmount){
		this.freeCreditAmount = freeCreditAmount;
	}

	public double getFreeCreditAmount(){
		return freeCreditAmount;
	}

	public void setCurrencyId(int currencyId){
		this.currencyId = currencyId;
	}

	public int getCurrencyId(){
		return currencyId;
	}

	public void setFreeAmount(double freeAmount){
		this.freeAmount = freeAmount;
	}

	public double getFreeAmount(){
		return freeAmount;
	}

	@Override
 	public String toString(){
		return 
			"BalanceItem{" + 
			"reserved_amount = '" + reservedAmount + '\'' + 
			",free_credit_amount = '" + freeCreditAmount + '\'' + 
			",currency_id = '" + currencyId + '\'' + 
			",free_amount = '" + freeAmount + '\'' + 
			"}";
		}
}