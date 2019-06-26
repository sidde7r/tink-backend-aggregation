package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Links{

	@JsonProperty("cancel")
	private Cancel cancel;

	@JsonProperty("token")
	private Token token;

	public Cancel getCancel(){
		return cancel;
	}

	public Token getToken(){
		return token;
	}
}