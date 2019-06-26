package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.entity.App;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class SubscriptionRequest{

	@JsonProperty("app")
	private App app;

	@JsonProperty("product")
	private Product product;

	public SubscriptionRequest(App app, Product product) {
		this.app = app;
		this.product = product;
	}

	public App getApp(){
		return app;
	}

	public Product getProduct(){
		return product;
	}
}