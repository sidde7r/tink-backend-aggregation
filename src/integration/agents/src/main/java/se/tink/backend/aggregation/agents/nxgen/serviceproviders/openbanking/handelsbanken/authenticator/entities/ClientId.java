package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class ClientId{

	@JsonProperty("$ref")
	private String ref;

	public ClientId(String ref){
		this.ref = ref;
	}

	public String getRef(){
		return ref;
	}
}