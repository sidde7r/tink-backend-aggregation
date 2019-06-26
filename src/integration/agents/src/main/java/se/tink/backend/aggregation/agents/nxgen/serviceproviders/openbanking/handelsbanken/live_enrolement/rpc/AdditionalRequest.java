package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class AdditionalRequest{

	@JsonProperty("product")
	private Product product;

	@JsonProperty("clientId")
	private String clientId;

	public Product getProduct(){
		return product;
	}

	public String getClientId(){
		return clientId;
	}


	public AdditionalRequest (String clientId, Product product) {
		this.clientId = clientId;
		this.product = product;
	}


}