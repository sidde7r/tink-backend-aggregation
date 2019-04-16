package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SectionHeaders{

	@JsonProperty("previousStatements")
	private String previousStatements;

	@JsonProperty("currentStatements")
	private String currentStatements;

	public String getPreviousStatements(){
		return previousStatements;
	}

	public String getCurrentStatements(){
		return currentStatements;
	}
}