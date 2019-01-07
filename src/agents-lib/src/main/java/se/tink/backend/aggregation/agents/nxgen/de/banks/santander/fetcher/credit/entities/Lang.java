package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Lang {

  @JsonProperty("DIALECTO_ISO")
  private String dIALECTOISO;

  @JsonProperty("IDIOMA_ISO")
  private String iDIOMAISO;

  public Lang(String dIALECTOISO, String iDIOMAISO) {
    this.dIALECTOISO = dIALECTOISO;
    this.iDIOMAISO = iDIOMAISO;
  }
}
