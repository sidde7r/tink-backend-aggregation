package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class ServicesItem{

	@JsonProperty("terms_of_service")
	private String termsOfService;

	@JsonProperty("name")
	private String name;

	@JsonProperty("enabled")
	private boolean enabled;

	public void setTermsOfService(String termsOfService){
		this.termsOfService = termsOfService;
	}

	public String getTermsOfService(){
		return termsOfService;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}

	public boolean isEnabled(){
		return enabled;
	}

	@Override
 	public String toString(){
		return 
			"ServicesItem{" + 
			"terms_of_service = '" + termsOfService + '\'' + 
			",name = '" + name + '\'' + 
			",enabled = '" + enabled + '\'' + 
			"}";
		}
}