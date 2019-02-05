package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Communication{
	private String type;
	private String message;

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setMessage(String message){
		this.message = message;
	}

	public String getMessage(){
		return message;
	}
}