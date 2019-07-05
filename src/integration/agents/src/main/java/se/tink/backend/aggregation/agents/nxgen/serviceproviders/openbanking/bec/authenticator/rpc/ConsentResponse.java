package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class ConsentResponse{

	@JsonProperty("consentId")
	private String consentId;

	@JsonProperty("_links")
	private Links links;

	@JsonProperty("consentStatus")
	private String consentStatus;

	public String getConsentId(){
		return consentId;
	}

	public Links getLinks(){
		return links;
	}

	public String getScaRedirect() {
		return links.getScaRedirect().getHref();
	}

	public String getConsentStatus(){
		return consentStatus;
	}
}