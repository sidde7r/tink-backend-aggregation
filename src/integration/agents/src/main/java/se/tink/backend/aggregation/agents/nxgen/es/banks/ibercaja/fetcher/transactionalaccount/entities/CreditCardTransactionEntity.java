package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CreditCardTransactionEntity {

    @JsonProperty("FechaOperacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfTransaction;

    @JsonProperty("Concepto")
    private String description;

    @JsonProperty("Importe")
    private double amount;

    public CreditCardTransaction toTinkTransaction() {
        return CreditCardTransaction.builder()
                .setAmount(new Amount(IberCajaConstants.currency, amount))
                .setDate(dateOfTransaction)
                .setDescription(description)
                .build();
    }
}
