package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.transactional.entity;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {
  private String currency;
  private BigDecimal value;

  public String getCurrency() {
    return currency;
  }

  public BigDecimal getValue() {
    return value;
  }
}
