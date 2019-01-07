package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TIPODEPRODUCTO {

  @JsonProperty("EMPRESA")
  private String eMPRESA;

  @JsonProperty("TIPO_DE_PRODUCTO")
  private String tIPODEPRODUCTO;

  public String geteMPRESA() {
    return eMPRESA;
  }

  public String gettIPODEPRODUCTO() {
    return tIPODEPRODUCTO;
  }
}
