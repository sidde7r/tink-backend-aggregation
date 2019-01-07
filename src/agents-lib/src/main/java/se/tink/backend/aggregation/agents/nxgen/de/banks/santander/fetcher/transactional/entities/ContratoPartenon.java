package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContratoPartenon {

  @JsonProperty("CENTRO")
  private CENTRO cENTRO;

  @JsonProperty("PRODUCTO")
  private String pRODUCTO;

  @JsonProperty("NUMERO_DE_CONTRATO")
  private String nUMERODECONTRATO;
}
