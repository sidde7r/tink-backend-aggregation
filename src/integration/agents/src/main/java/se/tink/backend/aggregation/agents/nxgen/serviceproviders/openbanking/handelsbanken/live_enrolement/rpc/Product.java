package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Product{

	@JsonProperty("name")
	private String name;

	public Product (String name) {
		this.name = name;
	}

	public String getName(){
		return name;
	}
}