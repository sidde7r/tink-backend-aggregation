package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class KindOfProduct {

  @JsonProperty("EMPRESA")
  private String company;

  @JsonProperty("TIPO_DE_PRODUCTO")
  private String kindOfProduct;

  public String getCompany() {
    return company;
  }

  public String getKindOfProduct() {
    return kindOfProduct;
  }
}
