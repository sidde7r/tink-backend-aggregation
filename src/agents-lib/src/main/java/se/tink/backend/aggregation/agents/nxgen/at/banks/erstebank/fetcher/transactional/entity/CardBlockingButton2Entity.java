package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardBlockingButton2Entity {

	@JsonProperty("actionPayload")
	private ActionPayloadEntity actionPayloadEntity;

	@JsonProperty("label")
	private String label;
}