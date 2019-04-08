package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public enum TransferStatusEntity {
  @JsonProperty("complete")
  COMPLETE,
  @JsonProperty("pending")
  PENDING,
  @JsonProperty("unknown")
  UNKNOWN,
  @JsonProperty("suspended")
  SUSPENDED,
  @JsonProperty("expired")
  EXPIRED,
}
