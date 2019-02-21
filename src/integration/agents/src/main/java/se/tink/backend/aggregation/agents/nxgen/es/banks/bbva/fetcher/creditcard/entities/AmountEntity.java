package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    private CurrencyEntity currency;
    private String amount;

    @JsonIgnore
    public Amount toTinkAmount(){
         return new Amount(currency.getId(), StringUtils.parseAmount(amount));
     }
}
