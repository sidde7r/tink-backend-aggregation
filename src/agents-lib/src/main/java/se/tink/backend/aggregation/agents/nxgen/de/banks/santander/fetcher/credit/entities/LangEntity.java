package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LangEntity {

  @JsonProperty("IDIOMA_ISO")
  private String iDIOMAISO;

  @JsonProperty("DIALECTO_ISO")
  private String dIALECTOISO;

  public LangEntity(String dIALECTOISO, String iDIOMAISO) {
    this.dIALECTOISO = dIALECTOISO;
    this.iDIOMAISO = iDIOMAISO;
  }
}
