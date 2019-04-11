package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionDetailsEntity {
    @JsonProperty("ConceptoMovimiento")
    private String transactionType;

    @JsonProperty("FechaOperacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfTransaction;

    @JsonProperty("Importe")
    private double amount;

    @JsonProperty("IdMovimientoPFM")
    private String idMovimientopfm;

    @JsonProperty("Registros")
    private List<String> descriptions;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(new Amount(IberCajaConstants.currency, amount))
                .setDate(dateOfTransaction)
                .setDescription(getDescription())
                .setExternalId(idMovimientopfm)
                .build();
    }

    @JsonIgnore
    private String getDescription() {
        if (descriptions != null) {
            // some transaction descriptions have a lot of whitespace that needs to be trimmed
            return descriptions.stream().map(this::trimWhiteSpace).collect(Collectors.joining(" "));
        }
        return transactionType;
    }

    @JsonIgnore
    private String trimWhiteSpace(String description) {
        return description.replaceAll("\\s+", " ");
    }
}
