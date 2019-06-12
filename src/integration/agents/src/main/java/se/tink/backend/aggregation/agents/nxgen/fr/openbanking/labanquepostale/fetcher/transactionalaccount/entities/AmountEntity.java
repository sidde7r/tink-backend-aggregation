package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
  private String currency;
  private String amount;

  public Amount toAmount(){
    return new Amount(currency, Double.parseDouble(amount));
  }

  public String getCurrency(){return currency;}
}
