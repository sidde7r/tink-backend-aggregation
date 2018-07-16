package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;

@JsonObject
public class CreditCardEntity {

    @JsonProperty("numeroTarjeta")
    private String cardNumber;

    @JsonProperty("datosLiquidacion")
    LiquidationDataEntity liquidationData;

    public CreditCardAccount toTinkCard(){
        return CreditCardAccount
                .builder(cardNumber, liquidationData.getPrepaidAmount(), liquidationData.getAvaliableCredit())
                .setAccountNumber(cardNumber)
                .build();
    }
}
