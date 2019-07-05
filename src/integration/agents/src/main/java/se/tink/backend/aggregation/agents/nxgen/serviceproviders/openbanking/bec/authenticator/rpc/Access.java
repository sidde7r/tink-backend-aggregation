package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Access{

	@JsonProperty("allPsd2")
	private String allPsd2;

	public String getAllPsd2(){
		return allPsd2;
	}

	public Access (String allPsd2) {
		this.allPsd2 = allPsd2;
	}
}