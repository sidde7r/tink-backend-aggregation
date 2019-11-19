package se.tink.backend.aggregation.agents.brokers.nordnet.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class PrivateFeed{

	@JsonProperty("hostname")
	private String hostname;

	@JsonProperty("encrypted")
	private boolean encrypted;

	@JsonProperty("port")
	private int port;

	public void setHostname(String hostname){
		this.hostname = hostname;
	}

	public String getHostname(){
		return hostname;
	}

	public void setEncrypted(boolean encrypted){
		this.encrypted = encrypted;
	}

	public boolean isEncrypted(){
		return encrypted;
	}

	public void setPort(int port){
		this.port = port;
	}

	public int getPort(){
		return port;
	}
}