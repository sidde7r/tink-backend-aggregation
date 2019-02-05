package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionDetailsEntity {

    @JsonProperty("ConceptoMovimiento")
    private String description;
    @JsonProperty("FechaOperacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfTransaction;
    @JsonProperty("Importe")
    private double amount;
    @JsonProperty("IdMovimientoPFM")
    private String idMovimientopfm;

    public Transaction toTinkTransaction(){
       return Transaction.builder().setAmount(new Amount(IberCajaConstants.currency, amount))
                .setDate(dateOfTransaction)
                .setDescription(description)
                .setExternalId(idMovimientopfm)
                .build();
    }
}
