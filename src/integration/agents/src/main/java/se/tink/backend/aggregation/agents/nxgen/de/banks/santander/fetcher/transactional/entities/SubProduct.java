package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubProduct {

  @JsonProperty("SUBTIPO_DE_PRODUCTO")
  private String productSubtitle;

  @JsonProperty("TIPO_DE_PRODUCTO")
  private KindOfProduct kindOfProduct;

  public String getProductSubtitle() {
    return productSubtitle;
  }

  public KindOfProduct getKindOfProduct() {
    return kindOfProduct;
  }
}
