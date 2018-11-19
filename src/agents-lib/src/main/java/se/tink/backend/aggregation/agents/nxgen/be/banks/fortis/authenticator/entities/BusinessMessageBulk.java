package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BusinessMessageBulk{

	@JsonProperty("globalIndicator")
	private Object globalIndicator;

	@JsonProperty("messages")
	private List<Object> messages;

	@JsonProperty("text")
	private Object text;

	@JsonProperty("pewCode")
	private Object pewCode;
}
