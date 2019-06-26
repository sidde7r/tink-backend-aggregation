package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Name{

	@JsonProperty("description")
	private String description;

	@JsonProperty("type")
	private String type;

	@JsonProperty("example")
	private String example;

	public Name(String description, String type, String example){
		this.description = description;
		this.type = type;
		this.example = example;
	}

	public String getDescription(){
		return description;
	}

	public String getType(){
		return type;
	}

	public String getExample(){
		return example;
	}
}