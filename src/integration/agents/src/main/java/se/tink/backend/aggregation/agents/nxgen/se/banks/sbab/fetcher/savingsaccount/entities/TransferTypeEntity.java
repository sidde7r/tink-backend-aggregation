package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public enum TransferTypeEntity {
  @JsonProperty("withdrawal")
  WITHDRAWAL,
  @JsonProperty("deposit")
  DEPOSIT,
  @JsonProperty("interest_rate")
  INTEREST_RATE,
  @JsonProperty("other")
  OTHER,
}
