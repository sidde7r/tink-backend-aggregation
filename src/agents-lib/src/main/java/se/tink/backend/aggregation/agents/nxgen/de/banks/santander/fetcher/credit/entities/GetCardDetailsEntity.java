package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetCardDetailsEntity {

  @JsonProperty("entrada")
  private EntradaEntity entrada;

  public GetCardDetailsEntity(String contractId, LangEntity langEntity, String pan) {
    entrada = new EntradaEntity(contractId, langEntity, pan);
  }
}
