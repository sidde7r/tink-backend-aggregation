package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Amount{

	@JsonProperty("rawValue")
	private double rawValue;

	@JsonProperty("formattedAmount")
	private String formattedAmount;

	@JsonProperty("stringRawValue")
	private String stringRawValue;

	public double getRawValue(){
		return rawValue;
	}

	public String getFormattedAmount(){
		return formattedAmount;
	}

	public String getStringRawValue(){
		return stringRawValue;
	}
}