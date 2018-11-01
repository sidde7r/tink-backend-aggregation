package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

	@JsonProperty("precision")
	private int precision;

	@JsonProperty("currency")
	private String currency;

	@JsonProperty("value")
	private int value;
}