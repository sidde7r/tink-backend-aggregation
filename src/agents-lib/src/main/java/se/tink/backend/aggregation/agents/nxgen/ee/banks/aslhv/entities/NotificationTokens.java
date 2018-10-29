package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class NotificationTokens{

	@JsonProperty("android")
	private String android;

	@JsonProperty("ios")
	private Object ios;

	public void setAndroid(String android){
		this.android = android;
	}

	public String getAndroid(){
		return android;
	}

	public void setIos(Object ios){
		this.ios = ios;
	}

	public Object getIos(){
		return ios;
	}

	@Override
 	public String toString(){
		return 
			"NotificationTokens{" + 
			"android = '" + android + '\'' + 
			",ios = '" + ios + '\'' + 
			"}";
		}
}