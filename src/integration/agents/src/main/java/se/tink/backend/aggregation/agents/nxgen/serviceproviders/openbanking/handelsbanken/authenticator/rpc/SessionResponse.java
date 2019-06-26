package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class SessionResponse{

	@JsonProperty("_links")
	private Links links;

	@JsonProperty("auto_start_token")
	private String autoStartToken;

	@JsonProperty("sleep_time")
	private int sleepTime;

	public Links getLinks(){
		return links;
	}

	public String getAutoStartToken(){
		return autoStartToken;
	}

	public int getSleepTime(){
		return sleepTime;
	}
}