package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Cancel{

	@JsonProperty("hints")
	private Hints hints;

	@JsonProperty("href")
	private String href;

	public Hints getHints(){
		return hints;
	}

	public String getHref(){
		return href;
	}
}