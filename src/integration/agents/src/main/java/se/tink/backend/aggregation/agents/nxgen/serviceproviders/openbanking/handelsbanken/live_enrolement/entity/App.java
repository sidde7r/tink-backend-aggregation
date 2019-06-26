package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.live_enrolement.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class App{

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("oauthRedirectURI")
	private String oauthRedirectURI;

	public App (String name, String description, String oauthRedirectURI) {
		this.name = name;
		this.description = description;
		this.oauthRedirectURI = oauthRedirectURI;
	}

	public String getName(){
		return name;
	}

	public String getDescription(){
		return description;
	}

	public String getOauthRedirectURI(){
		return oauthRedirectURI;
	}
}