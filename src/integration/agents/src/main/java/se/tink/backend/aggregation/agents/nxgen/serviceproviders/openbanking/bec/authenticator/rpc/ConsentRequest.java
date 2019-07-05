package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class ConsentRequest{

	@JsonProperty("access")
	private Access access;

	@JsonProperty("combinedServiceIndicator")
	private String combinedServiceIndicator;

	@JsonProperty("validUntil")
	private String validUntil;

	@JsonProperty("recurringIndicator")
	private String recurringIndicator;

	@JsonProperty("frequencyPerDay")
	private int frequencyPerDay;

	public Access getAccess(){
		return access;
	}

	public String getCombinedServiceIndicator(){
		return combinedServiceIndicator;
	}

	public String getValidUntil(){
		return validUntil;
	}

	public String getRecurringIndicator(){
		return recurringIndicator;
	}

	public int getFrequencyPerDay(){
		return frequencyPerDay;
	}

	public ConsentRequest (Access access, String combinedServiceIndicator, String validUntil, String recurringIndicator, int frequencyPerDay) {
		this.access = access;
		this.combinedServiceIndicator = combinedServiceIndicator;
		this.frequencyPerDay = frequencyPerDay;
		this.recurringIndicator = recurringIndicator;
		this.validUntil = validUntil;
	}

}