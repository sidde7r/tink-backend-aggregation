package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CENTRO {

  @JsonProperty("CENTRO")
  private String cENTRO;

  @JsonProperty("EMPRESA")
  private String eMPRESA;
}
