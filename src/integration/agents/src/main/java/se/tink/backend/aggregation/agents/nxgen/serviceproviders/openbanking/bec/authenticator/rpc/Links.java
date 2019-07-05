package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Links{

	@JsonProperty("scaStatus")
	private ScaStatus scaStatus;

	@JsonProperty("scaRedirect")
	private ScaRedirect scaRedirect;

	@JsonProperty("self")
	private Self self;

	@JsonProperty("status")
	private Status status;

	public ScaStatus getScaStatus(){
		return scaStatus;
	}

	public ScaRedirect getScaRedirect(){
		return scaRedirect;
	}

	public Self getSelf(){
		return self;
	}

	public Status getStatus(){
		return status;
	}
}