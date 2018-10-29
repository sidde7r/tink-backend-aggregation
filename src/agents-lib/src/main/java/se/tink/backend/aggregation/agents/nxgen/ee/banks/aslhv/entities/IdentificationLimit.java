package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class IdentificationLimit{

	@JsonProperty("amount")
	private Object amount;

	@JsonProperty("enabled")
	private boolean enabled;

	@JsonProperty("free_amount")
	private Object freeAmount;

	public void setAmount(Object amount){
		this.amount = amount;
	}

	public Object getAmount(){
		return amount;
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}

	public boolean isEnabled(){
		return enabled;
	}

	public void setFreeAmount(Object freeAmount){
		this.freeAmount = freeAmount;
	}

	public Object getFreeAmount(){
		return freeAmount;
	}

	@Override
 	public String toString(){
		return 
			"IdentificationLimit{" + 
			"amount = '" + amount + '\'' + 
			",enabled = '" + enabled + '\'' + 
			",free_amount = '" + freeAmount + '\'' + 
			"}";
		}
}