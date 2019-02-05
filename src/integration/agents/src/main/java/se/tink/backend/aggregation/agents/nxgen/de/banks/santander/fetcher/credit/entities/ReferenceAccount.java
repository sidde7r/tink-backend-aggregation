package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReferenceAccount {

  @JsonProperty("CODBBAN")
  private String cODBBAN;

  @JsonProperty("DIGITO_DE_CONTROL")
  private String dIGITODECONTROL;

  @JsonProperty("PAIS")
  private String pAIS;
}
