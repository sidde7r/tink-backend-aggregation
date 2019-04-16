package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Sort{

	@JsonProperty("values")
	private List<ValuesItem> values;

	@JsonProperty("header")
	private String header;

	public List<ValuesItem> getValues(){
		return values;
	}

	public String getHeader(){
		return header;
	}
}