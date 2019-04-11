package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.DateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class CardTransactionEntity {

    @JsonProperty("importeMovimiento")
    private AmountEntity movementAmount;

    @JsonProperty("fechaMovimiento")
    private DateEntity date;

    @JsonProperty("lugarMovimiento")
    private String description;

    @JsonProperty("descripcionMovimiento")
    private String type;

    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(movementAmount.toTinkAmount())
                .setDate(date.toJavaLangDate())
                .setDescription(description)
                .setRawDetails(this)
                .build();
    }
}
