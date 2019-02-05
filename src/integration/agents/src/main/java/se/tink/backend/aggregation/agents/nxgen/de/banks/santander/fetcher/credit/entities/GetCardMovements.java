package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCardMovements {

  @JsonProperty("entrada")
  private Entrada entrada;

  public GetCardMovements(Entrada entrada) {
    this.entrada = entrada;
  }
}
