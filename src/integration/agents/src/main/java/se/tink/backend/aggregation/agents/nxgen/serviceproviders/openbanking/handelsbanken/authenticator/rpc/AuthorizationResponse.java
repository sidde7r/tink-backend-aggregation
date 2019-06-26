package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities.ScaMethodsItem;

import javax.annotation.Generated;
import java.util.List;

@Generated("com.robohorse.robopojogenerator")
public class AuthorizationResponse{

	@JsonProperty("consentId")
	private String consentId;

	@JsonProperty("scaMethods")
	private List<ScaMethodsItem> scaMethods;

	@JsonProperty("consentStatus")
	private String consentStatus;

	public String getConsentId(){
		return consentId;
	}

	public List<ScaMethodsItem> getScaMethods(){
		return scaMethods;
	}

	public String getConsentStatus(){
		return consentStatus;
	}
}