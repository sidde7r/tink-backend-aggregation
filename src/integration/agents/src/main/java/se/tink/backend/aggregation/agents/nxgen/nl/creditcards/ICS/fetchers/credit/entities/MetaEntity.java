package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MetaEntity {
  @JsonProperty("TotalPages")
  private int totalPages;

  @JsonProperty("FirstAvailableDateTime")
  private Date firstAvailableDateTime;

  @JsonProperty("LastAvailableDateTime")
  private Date lastAvailableDateTime;
}
