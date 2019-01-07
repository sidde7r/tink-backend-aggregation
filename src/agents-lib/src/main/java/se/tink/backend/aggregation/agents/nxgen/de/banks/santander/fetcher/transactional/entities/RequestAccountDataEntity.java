package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RequestAccountDataEntity {
  @JsonProperty("getAccountServices_LA")
  private AccountQueryParameters accountQueryParameters = new AccountQueryParameters();

  public String toJson() {
    try {
      return new ObjectMapper().writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
